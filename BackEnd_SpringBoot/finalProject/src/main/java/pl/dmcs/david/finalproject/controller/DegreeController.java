package pl.dmcs.david.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.david.finalproject.model.Degree;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.payload.DegreeRequest;
import pl.dmcs.david.finalproject.payload.MessageResponse;
import pl.dmcs.david.finalproject.repository.DegreeRepository;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import pl.dmcs.david.finalproject.repository.SubjectRepository;

import java.util.List;

/**
 * Controller for managing Degrees (Careers).
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/degrees")
public class DegreeController {

    private final DegreeRepository degreeRepository;
    private final AppUserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public DegreeController(DegreeRepository degreeRepository, AppUserRepository userRepository, SubjectRepository subjectRepository) {
        this.degreeRepository = degreeRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Devuelve todas las carreras.
     * Cualquier usuario con un token válido (Estudiante, Profesor o Admin) puede verlo.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Degree>> getAllDegrees() {
        return ResponseEntity.ok(degreeRepository.findAll());
    }

    /**
     * Crea una carrera nueva.
     * SOLO accesible para el Admin.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDegree(@RequestBody DegreeRequest request) {

        if (degreeRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Degree name already exists!"));
        }

        // Crear una nueva entidad Degree y rellenar sus campos
        Degree degree = new Degree();
        degree.setName(request.getName());
        degree.setFaculty(request.getFaculty());
        degree.setTotalCredits(request.getTotalCredits());
        degree.setDurationYears(request.getDurationYears());
        
        degreeRepository.save(degree);

        return ResponseEntity.ok(new MessageResponse("Degree created successfully!"));
    }

    /**
     * Elimina una carrera existente.
     * SOLO accesible para el Admin.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDegree(@PathVariable Long id) {
        Degree degree = degreeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Degree not found."));

        // 1. Limpiar referencias en usuarios (estudiantes o profesores)
        List<AppUser> usersWithDegree = userRepository.findAll().stream()
                .filter(u -> u.getDegree() != null && u.getDegree().getId().equals(id))
                .toList();
        for (AppUser u : usersWithDegree) {
            u.setDegree(null);
            userRepository.save(u);
        }

        // 2. Limpiar referencias en asignaturas (subjects ManyToMany)
        List<Subject> subjectsWithDegree = subjectRepository.findAll().stream()
                .filter(s -> s.getDegrees().contains(degree))
                .toList();
        for (Subject s : subjectsWithDegree) {
            s.getDegrees().remove(degree);
            subjectRepository.save(s);
        }

        // 3. Eliminar la carrera
        degreeRepository.delete(degree);

        return ResponseEntity.ok(new MessageResponse("Degree deleted successfully!"));
    }

    /**
     * Obtiene todos los usuarios del sistema para permitir la asignacion de carreras.
     * Accesible para Admin y Profesores.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<AppUser>> getAssignableUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Asigna un usuario a una carrera específica.
     * Accesible para Admin y Profesores.
     */
    @PutMapping("/{degreeId}/assign-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> assignUserToDegree(@PathVariable Long degreeId, @PathVariable Long userId) {
        Degree degree = degreeRepository.findById(degreeId)
                .orElseThrow(() -> new RuntimeException("Error: Degree not found."));

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        user.setDegree(degree);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User " + user.getUsername() + " successfully assigned to " + degree.getName() + "!"));
    }

    /**
     * Actualiza los detalles de una carrera existente.
     * SOLO accesible para el Admin.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDegree(@PathVariable Long id, @RequestBody DegreeRequest request) {
        Degree degree = degreeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Degree not found."));

        if (!degree.getName().equalsIgnoreCase(request.getName())) {
            if (degreeRepository.findByName(request.getName()).isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: El nombre de la carrera ya está en uso por otra carrera."));
            }
        }

        degree.setName(request.getName());
        degree.setFaculty(request.getFaculty());
        degree.setTotalCredits(request.getTotalCredits());
        degree.setDurationYears(request.getDurationYears());

        degreeRepository.save(degree);
        return ResponseEntity.ok(new MessageResponse("Degree updated successfully!"));
    }
}