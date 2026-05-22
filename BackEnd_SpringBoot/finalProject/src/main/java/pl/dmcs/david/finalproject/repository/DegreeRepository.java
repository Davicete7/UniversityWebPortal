package pl.dmcs.david.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dmcs.david.finalproject.model.Degree;
import java.util.Optional;

/**
 * Repository interface for Degree entity.
 */
@Repository
public interface DegreeRepository extends JpaRepository<Degree, Long> {

    /**
     * Finds a degree by its name.
     */
    Optional<Degree> findByName(String name);
}