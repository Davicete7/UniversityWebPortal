package pl.dmcs.david.finalproject.payload;

/**
 * Objeto de transferencia de datos (DTO) para recibir solicitudes de modificación de usuarios por el administrador.
 */
public class UserUpdateRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String academicEmail;
    private String password; // Opcional, si se proporciona se encriptará y actualizará
    private String role; // Ej. ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN
    private Long degreeId; // Identificador opcional de la carrera (Degree)
    private String phoneNumber;
    private String bio;

    public UserUpdateRequest() {}

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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getDegreeId() { return degreeId; }
    public void setDegreeId(Long degreeId) { this.degreeId = degreeId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
