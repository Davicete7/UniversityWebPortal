package pl.dmcs.david.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dmcs.david.finalproject.model.Subject;
import java.util.List;

/**
 * Repository interface for Subject entity.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Retrieves all subjects assigned to a specific teacher.
     * Spring automatically traverses the relationship (assignedTeacher.id).
     */
    List<Subject> findByAssignedTeacherId(Long teacherId);
}
