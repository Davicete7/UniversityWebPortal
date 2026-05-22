package pl.dmcs.david.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dmcs.david.finalproject.model.Grade;
import java.util.List;

/**
 * Repository interface for Grade entity.
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    /**
     * Retrieves all grades belonging to a specific student.
     * This fulfills the requirement: "Student can list check the grades assigned"
     */
    List<Grade> findByGradedStudentId(Long studentId);

    /**
     * Retrieves all grades given for a specific subject.
     */
    List<Grade> findByEvaluatedSubjectId(Long subjectId);
}