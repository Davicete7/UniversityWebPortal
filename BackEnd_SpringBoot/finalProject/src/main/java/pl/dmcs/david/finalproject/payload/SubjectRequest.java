package pl.dmcs.david.finalproject.payload;

/**
 * DTO for creating a new Subject (Asignatura).
 */
public class SubjectRequest {
    private String name;

    // Lista de identificadores de las carreras a las que pertenece la asignatura
    private java.util.List<Long> degreeIds;

    // Identificador del profesor asignado a la asignatura (opcional)
    private Long teacherId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public java.util.List<Long> getDegreeIds() { return degreeIds; }
    public void setDegreeIds(java.util.List<Long> degreeIds) { this.degreeIds = degreeIds; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}