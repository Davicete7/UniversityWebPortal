package pl.dmcs.david.finalproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dmcs.david.finalproject.model.Role;
import java.util.Optional;

/**
 * Repository interface for Role entity.
 * Provides basic CRUD operations and custom queries for roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its exact name (e.g., "ROLE_STUDENT").
     * Uses Optional to avoid NullPointerExceptions if the role doesn't exist.
     */
    Optional<Role> findByRoleName(String roleName);
}