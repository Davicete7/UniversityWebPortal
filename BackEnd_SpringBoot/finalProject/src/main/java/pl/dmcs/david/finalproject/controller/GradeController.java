package pl.dmcs.david.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.model.Grade;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.payload.GradeRequest;
import pl.dmcs.david.finalproject.payload.MessageResponse;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import pl.dmcs.david.finalproject.repository.SubjectRepository;
import pl.dmcs.david.finalproject.repository.GradeRepository;
import pl.dmcs.david.finalproject.service.GradeService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing student grades and teacher reviews.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;
    private final AppUserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;

    public GradeController(GradeService gradeService, AppUserRepository userRepository,
                           SubjectRepository subjectRepository, GradeRepository gradeRepository) {
        this.gradeService = gradeService;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> assignGrade(@RequestBody GradeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser teacher = userRepository.findByUsername(authentication.getName()).get();

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (subject.getAssignedTeacher() == null || !subject.getAssignedTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Unauthorized to grade this subject."));
        }

        AppUser student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        // Validar que el estudiante no tenga ya una calificación final asignada para esta asignatura
        boolean alreadyGraded = gradeRepository.findAll().stream()
                .anyMatch(g -> g.getGradedStudent().getId().equals(request.getStudentId()) &&
                               g.getEvaluatedSubject().getId().equals(request.getSubjectId()));
        if (alreadyGraded) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Student already has a final grade for this subject."));
        }

        Grade grade = new Grade();
        grade.setGradeValue(request.getGradeValue());
        grade.setGradedStudent(student);
        grade.setEvaluatedSubject(subject);
        // Seteamos el profesor que evalúa, lo cual es obligatorio en la base de datos
        grade.setGradedByTeacher(teacher);

        gradeService.assignGrade(grade);
        return ResponseEntity.ok(new MessageResponse("Grade assigned successfully!"));

    }

    /**
     * Allows a teacher to see all grades they have given for a specific subject.
     */
    @GetMapping("/teacher/subject/{subjectId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getGradesBySubject(@PathVariable Long subjectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser teacher = userRepository.findByUsername(authentication.getName()).get();

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (subject.getAssignedTeacher() == null || !subject.getAssignedTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: This is not your subject."));
        }

        List<Grade> grades = gradeRepository.findAll().stream()
                .filter(g -> g.getEvaluatedSubject().getId().equals(subjectId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(grades);
    }

    @GetMapping("/my-grades")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Grade>> getMyGrades() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser student = userRepository.findByUsername(authentication.getName()).get();
        
        List<Grade> actualGrades = gradeService.getGradesForStudent(student.getId());
        List<Grade> result = new java.util.ArrayList<>(actualGrades);

        // Generar notas virtuales (con valor nulo/Pending) para las asignaturas matriculadas sin nota
        for (Subject subject : student.getEnrolledSubjects()) {
            boolean hasGrade = actualGrades.stream()
                    .anyMatch(g -> g.getEvaluatedSubject().getId().equals(subject.getId()));
            if (!hasGrade) {
                Grade virtualGrade = new Grade();
                virtualGrade.setGradeValue(null);
                virtualGrade.setEvaluatedSubject(subject);
                virtualGrade.setGradedStudent(student);
                virtualGrade.setGradedByTeacher(subject.getAssignedTeacher());
                result.add(virtualGrade);
            }
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Permite a un profesor actualizar la calificación final de un estudiante.
     */
    @PutMapping("/{gradeId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateGrade(@PathVariable Long gradeId, @RequestBody GradeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser teacher = userRepository.findByUsername(authentication.getName()).get();

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Error: Grade not found."));

        // Verificar que el profesor es quien asignó la nota o es el profesor de la asignatura
        if (!grade.getGradedByTeacher().getId().equals(teacher.getId()) &&
            (grade.getEvaluatedSubject().getAssignedTeacher() == null ||
             !grade.getEvaluatedSubject().getAssignedTeacher().getId().equals(teacher.getId()))) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not authorized to edit this grade."));
        }

        grade.setGradeValue(request.getGradeValue());
        grade.setDateGiven(new java.util.Date());
        gradeRepository.save(grade);

        return ResponseEntity.ok(new MessageResponse("Grade updated successfully!"));
    }
}