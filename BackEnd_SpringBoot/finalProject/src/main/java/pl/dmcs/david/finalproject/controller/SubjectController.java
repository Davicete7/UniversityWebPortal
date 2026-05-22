package pl.dmcs.david.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.model.Degree;
import pl.dmcs.david.finalproject.model.Grade;
import pl.dmcs.david.finalproject.payload.MessageResponse;
import pl.dmcs.david.finalproject.payload.SubjectRequest;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import pl.dmcs.david.finalproject.repository.SubjectRepository;
import pl.dmcs.david.finalproject.repository.DegreeRepository;
import pl.dmcs.david.finalproject.repository.GradeRepository;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Controller for managing Subjects, teacher assignments, and student lists.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final AppUserRepository userRepository;
    private final DegreeRepository degreeRepository;
    private final GradeRepository gradeRepository;

    public SubjectController(SubjectRepository subjectRepository, AppUserRepository userRepository, DegreeRepository degreeRepository, GradeRepository gradeRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.degreeRepository = degreeRepository;
        this.gradeRepository = gradeRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSubject(@RequestBody SubjectRequest request) {
        if (request.getDegreeIds() == null || request.getDegreeIds().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: La asignatura debe pertenecer a al menos una carrera universitaria."));
        }

        // Crear una nueva asignatura y configurar sus propiedades
        Subject subject = new Subject();
        subject.setSubjectName(request.getName());

        // Asignar las carreras
        Set<Degree> degrees = new HashSet<>(degreeRepository.findAllById(request.getDegreeIds()));
        subject.setDegrees(degrees);

        // Asignar el profesor si se especifica
        if (request.getTeacherId() != null) {
            AppUser teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Error: Teacher not found."));
            subject.setAssignedTeacher(teacher);
        }

        subjectRepository.save(subject);
        return ResponseEntity.ok(new MessageResponse("Subject created successfully!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSubject(@PathVariable Long id, @RequestBody SubjectRequest request) {
        if (request.getDegreeIds() == null || request.getDegreeIds().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: La asignatura debe pertenecer a al menos una carrera universitaria."));
        }

        // Buscar la asignatura existente para su edición
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        subject.setSubjectName(request.getName());

        // Actualizar las carreras asignadas
        Set<Degree> degrees = new HashSet<>(degreeRepository.findAllById(request.getDegreeIds()));
        subject.setDegrees(degrees);

        // Actualizar el profesor asignado
        if (request.getTeacherId() != null) {
            AppUser teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Error: Teacher not found."));
            subject.setAssignedTeacher(teacher);
        } else {
            subject.setAssignedTeacher(null);
        }

        subjectRepository.save(subject);
        return ResponseEntity.ok(new MessageResponse("Subject updated successfully!"));
    }

    /**
     * Permite a un profesor auto-asignarse a una asignatura de su carrera.
     */
    @PostMapping("/{subjectId}/self-assign")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> teacherSelfAssign(@PathVariable Long subjectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser teacher = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Error: Teacher not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (teacher.getDegree() == null || !subject.getDegrees().contains(teacher.getDegree())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes asignarte a asignaturas asociadas a tu carrera universitaria."));
        }

        if (subject.getAssignedTeacher() != null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: This subject already has a teacher."));
        }

        subject.setAssignedTeacher(teacher);
        subjectRepository.save(subject);
        return ResponseEntity.ok(new MessageResponse("You have successfully assigned yourself to " + subject.getSubjectName()));
    }

    /**
     * Allows a Teacher to unassign themselves from a subject.
     */
    @DeleteMapping("/{subjectId}/self-unassign")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> teacherSelfUnassign(@PathVariable Long subjectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser teacher = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Error: Teacher not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (subject.getAssignedTeacher() == null || !subject.getAssignedTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not the teacher of this subject."));
        }

        subject.setAssignedTeacher(null);
        subjectRepository.save(subject);
        return ResponseEntity.ok(new MessageResponse("You have unassigned yourself from " + subject.getSubjectName()));
    }

    /**
     * Returns the list of students enrolled in a specific subject.
     * Accessible by the Teacher of the subject or the Admin.
     */
    @GetMapping("/{subjectId}/students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> getStudentsInSubject(@PathVariable Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        // In a real scenario, you might want to return a specific DTO to avoid sensitive data
        return ResponseEntity.ok(subject.getEnrolledStudents());
    }

    /**
     * Elimina una asignatura del sistema junto con todas las relaciones asociadas.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        // 1. Limpiar las matrículas de los estudiantes en esta asignatura
        List<AppUser> enrolledUsers = userRepository.findAll().stream()
                .filter(u -> u.getEnrolledSubjects().contains(subject))
                .toList();
        for (AppUser u : enrolledUsers) {
            u.getEnrolledSubjects().remove(subject);
            userRepository.save(u);
        }

        // 2. Eliminar las calificaciones asociadas a la asignatura
        List<Grade> grades = gradeRepository.findByEvaluatedSubjectId(id);
        gradeRepository.deleteAll(grades);

        // 3. Limpiar las relaciones ManyToMany con Degrees
        subject.getDegrees().clear();
        subjectRepository.save(subject);

        // 4. Eliminar la asignatura
        subjectRepository.delete(subject);

        return ResponseEntity.ok(new MessageResponse("Subject deleted successfully!"));
    }
}