package pl.dmcs.david.finalproject.payload;

import java.util.Set;

/**
 * Objeto de Transferencia de Datos (DTO) para las solicitudes de registro.
 * Contiene todos los detalles personales y académicos necesarios para crear un usuario.
 */
public class SignupRequest {
    // Nombre de usuario único para la autenticación
    private String username;

    // Nombre de pila del usuario
    private String firstName;

    // Apellido o apellidos del usuario
    private String lastName;

    // Contraseña temporal asignada por el administrador
    private String password;

    // Correo electrónico académico del usuario (debe ser único)
    private String academicEmail;

    // Nombre de la carrera o programa de estudios en el que se matricula (opcional, solo para estudiantes)
    private String degreeName;

    // Rol único asignado al sistema (por ejemplo: ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN)
    // Se ha cambiado a singular 'role' para que coincida con el payload enviado por el frontend en Angular
    private String role;

    // Constructor por defecto requerido para la deserialización de JSON a objeto Java
    public SignupRequest() {}

    // Métodos Getter y Setter para acceder y modificar las propiedades de la solicitud

    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getFirstName() { 
        return firstName; 
    }
    
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }

    public String getLastName() { 
        return lastName; 
    }
    
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }

    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getAcademicEmail() { 
        return academicEmail; 
    }
    
    public void setAcademicEmail(String academicEmail) { 
        this.academicEmail = academicEmail; 
    }

    public String getDegreeName() { 
        return degreeName; 
    }
    
    public void setDegreeName(String degreeName) { 
        this.degreeName = degreeName; 
    }

    public String getRole() { 
        return role; 
    }
    
    public void setRole(String role) { 
        this.role = role; 
    }
}