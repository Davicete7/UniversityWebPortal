package pl.dmcs.david.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.payload.MessageResponse;
import pl.dmcs.david.finalproject.payload.ProfileUpdateRequest;
import pl.dmcs.david.finalproject.repository.AppUserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestionar las operaciones del perfil de usuario.
 * Permite a cualquier usuario autenticado (estudiante, profesor o administrador)
 * consultar sus datos personales y actualizarlos, incluyendo la contraseña y la foto de perfil.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
public class ProfileController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder encoder;

    /**
     * Inyección de dependencias mediante constructor para asegurar la inmutabilidad
     * y facilitar las pruebas unitarias.
     */
    public ProfileController(AppUserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    /**
     * Recupera el perfil del usuario actualmente autenticado en el contexto de seguridad.
     * Mapea los datos a un Map para evitar la exposición de la contraseña en texto plano
     * o hash, y prevenir recursiones cíclicas en la serialización JSON.
     */
    @GetMapping
    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", user.getId());
        profileData.put("username", user.getUsername());
        profileData.put("firstName", user.getFirstName());
        profileData.put("lastName", user.getLastName());
        profileData.put("academicEmail", user.getAcademicEmail());
        profileData.put("phoneNumber", user.getPhoneNumber());
        profileData.put("bio", user.getBio());
        profileData.put("avatar", user.getAvatar());
        profileData.put("role", user.getUserRole() != null ? user.getUserRole().getRoleName() : null);
        
        if (user.getDegree() != null) {
            profileData.put("degreeName", user.getDegree().getName());
        }

        return ResponseEntity.ok(profileData);
    }

    /**
     * Actualiza los datos del perfil del usuario autenticado.
     * Solo modifica aquellos campos que no sean nulos o vacíos en la solicitud.
     * En caso de recibir una nueva contraseña, esta se encripta mediante BCrypt.
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Validación y actualización parcial de datos básicos
        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().trim().isEmpty()) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null && !updateRequest.getLastName().trim().isEmpty()) {
            user.setLastName(updateRequest.getLastName());
        }
        
        // El teléfono y la biografía pueden vaciarse o actualizarse
        user.setPhoneNumber(updateRequest.getPhoneNumber());
        user.setBio(updateRequest.getBio());

        // Actualización del avatar en Base64 (almacenado como texto en base de datos)
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }

        // Si se proporciona una nueva contraseña, se encripta antes de guardarse
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            if (updateRequest.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Password must be at least 6 characters."));
            }
            user.setPassword(encoder.encode(updateRequest.getPassword()));
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profile updated successfully!"));
    }
}
