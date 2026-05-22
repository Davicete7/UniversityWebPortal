package pl.dmcs.david.finalproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Main user entity for the application.
 */
@Entity
@Table(name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "academicEmail")
        })
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String academicEmail;

    @Column(nullable = false)
    private String password;

    // CHANGED: From @ManyToMany to @ManyToOne
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role userRole;

    @ManyToOne
    @JoinColumn(name = "degree_id")
    private Degree degree;

    @ManyToMany
    @JoinTable(
            name = "student_enrollments",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @JsonIgnore
    private Set<Subject> enrolledSubjects = new HashSet<>();

    // Foto de perfil del usuario en formato Base64 (almacenada como TEXT para permitir strings largos)
    @Column(columnDefinition = "TEXT")
    private String avatar;

    // Número de teléfono personal del usuario
    private String phoneNumber;

    // Biografía o descripción académica del usuario
    private String bio;

    public AppUser() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAcademicEmail() { return academicEmail; }
    public void setAcademicEmail(String academicEmail) { this.academicEmail = academicEmail; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // CHANGED GETTER AND SETTER
    public Role getUserRole() { return userRole; }
    public void setUserRole(Role userRole) { this.userRole = userRole; }

    public Degree getDegree() { return degree; }
    public void setDegree(Degree degree) { this.degree = degree; }
    public Set<Subject> getEnrolledSubjects() { return enrolledSubjects; }
    public void setEnrolledSubjects(Set<Subject> enrolledSubjects) { this.enrolledSubjects = enrolledSubjects; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}