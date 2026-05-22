package pl.dmcs.david.finalproject.payload;

/**
 * DTO for creating a new Degree (Carrera).
 */
public class DegreeRequest {
    private String name;
    private String faculty;
    private Integer totalCredits;
    private Integer durationYears;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }

    public Integer getDurationYears() { return durationYears; }
    public void setDurationYears(Integer durationYears) { this.durationYears = durationYears; }
}