package pl.dmcs.david.finalproject.model;

import jakarta.persistence.*;

/**
 * Entity representing the user roles in the system (e.g., ROLE_STUDENT, ROLE_TEACHER).
 * Required for JWT security and role-based access control.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roleName;

    public Role() {}

    public Role(String roleName) {
        this.roleName = roleName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}