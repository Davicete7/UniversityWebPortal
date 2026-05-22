package pl.dmcs.david.finalproject.service;

import org.springframework.stereotype.Service;
import pl.dmcs.david.finalproject.model.Grade;
import pl.dmcs.david.finalproject.repository.GradeRepository;
import java.util.Date;
import java.util.List;

/**
 * Service class handling the logic for grading students.
 */
@Service
public class GradeService {

    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    /**
     * Assigns a grade to a student and automatically sets the grading date.
     */
    public Grade assignGrade(Grade grade) {
        // Business logic: Set the exact moment the grade was given
        grade.setDateGiven(new Date());

        return gradeRepository.save(grade);
    }

    /**
     * Retrieves all grades for a specific student ID.
     */
    public List<Grade> getGradesForStudent(Long studentId) {
        return gradeRepository.findByGradedStudentId(studentId);
    }
}