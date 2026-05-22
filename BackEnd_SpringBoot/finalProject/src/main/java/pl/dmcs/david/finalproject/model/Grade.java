package pl.dmcs.david.finalproject.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entity representing a grade given to a student by a teacher.
 */
@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double gradeValue;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateGiven;

    private String examType; // e.g., "Final Exam", "Project", "Partial"

    /**
     * The student receiving the grade.
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser gradedStudent;

    /**
     * The subject the grade belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject evaluatedSubject;

    /**
     * The teacher who assigned this specific grade.
     */
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private AppUser gradedByTeacher;

    public Grade() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getGradeValue() { return gradeValue; }
    public void setGradeValue(Double gradeValue) { this.gradeValue = gradeValue; }

    public Date getDateGiven() { return dateGiven; }
    public void setDateGiven(Date dateGiven) { this.dateGiven = dateGiven; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public AppUser getGradedStudent() { return gradedStudent; }
    public void setGradedStudent(AppUser gradedStudent) { this.gradedStudent = gradedStudent; }

    public Subject getEvaluatedSubject() { return evaluatedSubject; }
    public void setEvaluatedSubject(Subject evaluatedSubject) { this.evaluatedSubject = evaluatedSubject; }

    public AppUser getGradedByTeacher() { return gradedByTeacher; }
    public void setGradedByTeacher(AppUser gradedByTeacher) { this.gradedByTeacher = gradedByTeacher; }
}