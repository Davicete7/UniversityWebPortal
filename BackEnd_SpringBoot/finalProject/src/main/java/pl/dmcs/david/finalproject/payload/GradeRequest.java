package pl.dmcs.david.finalproject.payload;

/**
 * Data Transfer Object containing the necessary information to assign a grade.
 */
public class GradeRequest {

    private Long studentId;
    private Long subjectId;
    private double gradeValue;

    // Getters and Setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public double getGradeValue() { return gradeValue; }
    public void setGradeValue(double gradeValue) { this.gradeValue = gradeValue; }
}