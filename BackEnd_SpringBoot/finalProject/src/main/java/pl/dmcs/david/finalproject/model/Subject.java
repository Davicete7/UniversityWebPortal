package pl.dmcs.david.finalproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Subject entity representing a university course.
 */
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectName;

    // Relación con el Profesor (un profesor puede impartir varias asignaturas)
    // Se establece nullable = true para permitir la creación de asignaturas sin profesor asignado inicialmente
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = true)
    private AppUser assignedTeacher;


    // Relación de muchos a muchos con carreras (Degrees). Una asignatura puede pertenecer a múltiples carreras.
    @ManyToMany
    @JoinTable(
            name = "subject_degrees",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "degree_id")
    )
    private Set<Degree> degrees = new HashSet<>();

    // Bidirectional Many-to-Many relationship with Students
    // mappedBy refers to the attribute name in the AppUser entity
    // @JsonIgnore is used to prevent infinite loops during JSON serialization
    @ManyToMany(mappedBy = "enrolledSubjects")
    @JsonIgnore
    private Set<AppUser> enrolledStudents = new HashSet<>();

    // Default constructor
    public Subject() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public AppUser getAssignedTeacher() {
        return assignedTeacher;
    }

    public void setAssignedTeacher(AppUser assignedTeacher) {
        this.assignedTeacher = assignedTeacher;
    }

    public Set<Degree> getDegrees() {
        return degrees;
    }

    public void setDegrees(Set<Degree> degrees) {
        this.degrees = degrees;
    }

    public Set<AppUser> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(Set<AppUser> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
}