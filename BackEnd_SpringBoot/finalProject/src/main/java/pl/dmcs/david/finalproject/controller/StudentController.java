package pl.dmcs.david.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import pl.dmcs.david.finalproject.repository.SubjectRepository;

import java.util.Set;

/**
 * Controller for student-specific operations like enrollment and classmate reviews.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final AppUserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public StudentController(AppUserRepository userRepository, SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @PostMapping("/enroll/{subjectId}")
    public ResponseEntity<?> enrollInSubject(@PathVariable Long subjectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser student = userRepository.findByUsername(authentication.getName()).get();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (student.getDegree() == null || !subject.getDegrees().contains(student.getDegree())) {
            return ResponseEntity.badRequest().body("Error: Esta asignatura no pertenece a tu grado universitario.");
        }

        if (student.getEnrolledSubjects().contains(subject)) {
            return ResponseEntity.badRequest().body("Error: Already enrolled.");
        }

        student.getEnrolledSubjects().add(subject);
        userRepository.save(student);
        return ResponseEntity.ok("Enrolled successfully in " + subject.getSubjectName());
    }

    /**
     * Permite a un estudiante desmatricularse de una asignatura (ahora deshabilitado por política de matrícula irrevocable).
     */
    @DeleteMapping("/unenroll/{subjectId}")
    public ResponseEntity<?> unenrollFromSubject(@PathVariable Long subjectId) {
        return ResponseEntity.badRequest().body("Error: La matrícula es irrevocable. Por favor, ponte en contacto con un administrador del sistema para desmatricularte.");
    }

    @GetMapping("/my-subjects")
    public ResponseEntity<Set<Subject>> getMySubjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser student = userRepository.findByUsername(authentication.getName()).get();
        return ResponseEntity.ok(student.getEnrolledSubjects());
    }

    /**
     * Allows a student to see their classmates in a specific subject.
     */
    @GetMapping("/subject/{subjectId}/classmates")
    public ResponseEntity<?> getClassmates(@PathVariable Long subjectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser student = userRepository.findByUsername(authentication.getName()).get();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        // Check if the student is actually enrolled in this subject
        if (!student.getEnrolledSubjects().contains(subject)) {
            return ResponseEntity.badRequest().body("Error: You are not enrolled in this subject.");
        }

        return ResponseEntity.ok(subject.getEnrolledStudents());
    }
}