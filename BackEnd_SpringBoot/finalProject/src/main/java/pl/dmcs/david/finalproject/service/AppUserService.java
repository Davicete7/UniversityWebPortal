package pl.dmcs.david.finalproject.service;

import org.springframework.stereotype.Service;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import java.util.List;

/**
 * Service class for handling user-related business logic (Students and Teachers).
 */
@Service
public class AppUserService {

    private final AppUserRepository userRepository;

    public AppUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by username.
     * Uses the Optional logic to throw an error if the user "box" is empty.
     */
    public AppUser getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found with username: " + username));
    }

    /**
     * Saves a new user, checking first if the username or email is already taken.
     */
    public AppUser registerUser(AppUser user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByAcademicEmail(user.getAcademicEmail())) {
            throw new RuntimeException("Error: Academic email is already in use!");
        }

        // Note: Password encryption (hashing) will be implemented here later
        // when we set up Spring Security and JWT.
        return userRepository.save(user);
    }
}
