package pl.dmcs.david.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dmcs.david.finalproject.model.AppUser;
import java.util.Optional;

/**
 * Repository interface for AppUser entity.
 * Handles database operations for both students and teachers.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Finds a user by their unique username. Crucial for the login process.
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Checks if a user exists with the given username. Useful for registration validation.
     */
    Boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given academic email.
     */
    Boolean existsByAcademicEmail(String academicEmail);
}