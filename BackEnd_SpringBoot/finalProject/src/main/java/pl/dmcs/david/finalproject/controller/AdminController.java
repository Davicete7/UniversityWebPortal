package pl.dmcs.david.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.model.Role;
import pl.dmcs.david.finalproject.model.Degree;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.model.Grade;
import pl.dmcs.david.finalproject.payload.MessageResponse;
import pl.dmcs.david.finalproject.payload.SignupRequest;
import pl.dmcs.david.finalproject.payload.UserUpdateRequest;
import pl.dmcs.david.finalproject.payload.GradeRequest;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import pl.dmcs.david.finalproject.repository.DegreeRepository;
import pl.dmcs.david.finalproject.repository.RoleRepository;
import pl.dmcs.david.finalproject.repository.SubjectRepository;
import pl.dmcs.david.finalproject.repository.GradeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Optional;

/**
 * Controller dedicated to Administrator actions.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DegreeRepository degreeRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder encoder;

    public AdminController(AppUserRepository userRepository, RoleRepository roleRepository,
                           DegreeRepository degreeRepository, SubjectRepository subjectRepository,
                           GradeRepository gradeRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.degreeRepository = degreeRepository;
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
        this.encoder = encoder;
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByAcademicEmail(signUpRequest.getAcademicEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        AppUser user = new AppUser();
        user.setUsername(signUpRequest.getUsername());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setAcademicEmail(signUpRequest.getAcademicEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        // Determinar rol y validar carrera
        String strRole = signUpRequest.getRole();
        if (strRole == null) {
            strRole = "role_student";
        }

        boolean isStudentOrTeacher = strRole.equalsIgnoreCase("role_student") || strRole.equalsIgnoreCase("role_teacher");
        if (isStudentOrTeacher) {
            if (signUpRequest.getDegreeName() == null || signUpRequest.getDegreeName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: You must select university degree"));
            }
            Optional<Degree> degreeOpt = degreeRepository.findByName(signUpRequest.getDegreeName());
            if (!degreeOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: The university degree you have selected doesnt exists."));
            }
            user.setDegree(degreeOpt.get());
        } else {
            if (signUpRequest.getDegreeName() != null) {
                degreeRepository.findByName(signUpRequest.getDegreeName()).ifPresent(user::setDegree);
            }
        }

        Role userRole;
        switch(strRole.toLowerCase()) {
            case "role_admin":
                userRole = roleRepository.findByRoleName("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                break;
            case "role_teacher":
                userRole = roleRepository.findByRoleName("ROLE_TEACHER")
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                break;
            default:
                userRole = roleRepository.findByRoleName("ROLE_STUDENT")
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        }

        user.setUserRole(userRole);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User created successfully by Admin!"));
    }

    /**
     * Devuelve una lista de todos los usuarios registrados en el sistema.
     */
    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Actualiza la información de perfil y académica de un usuario por su ID.
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Comprobamos que el username no esté duplicado si se ha modificado
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        // Comprobamos que el correo electrónico académico no esté duplicado
        if (!user.getAcademicEmail().equals(request.getAcademicEmail()) && userRepository.existsByAcademicEmail(request.getAcademicEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Actualizar datos básicos
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAcademicEmail(request.getAcademicEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBio(request.getBio());

        // Actualizar contraseña si se proporciona una nueva
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(encoder.encode(request.getPassword()));
        }

        // Determinar rol y validar carrera (Degree)
        String strRole = (request.getRole() != null) ? request.getRole() : user.getUserRole().getRoleName();
        boolean isStudentOrTeacher = strRole.equalsIgnoreCase("role_student") || strRole.equalsIgnoreCase("role_teacher") || strRole.equalsIgnoreCase("student") || strRole.equalsIgnoreCase("teacher");

        if (isStudentOrTeacher) {
            if (request.getDegreeId() == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: El grado universitario es obligatorio para estudiantes y profesores."));
            }
            Degree degree = degreeRepository.findById(request.getDegreeId())
                    .orElseThrow(() -> new RuntimeException("Error: El grado universitario no existe."));
            user.setDegree(degree);
        } else {
            if (request.getDegreeId() != null) {
                Degree degree = degreeRepository.findById(request.getDegreeId())
                        .orElseThrow(() -> new RuntimeException("Error: El grado universitario no existe."));
                user.setDegree(degree);
            } else {
                user.setDegree(null);
            }
        }

        // Actualizar Rol
        if (request.getRole() != null) {
            Role userRole;
            switch(strRole.toLowerCase()) {
                case "role_admin":
                    userRole = roleRepository.findByRoleName("ROLE_ADMIN")
                            .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                    break;
                case "role_teacher":
                    userRole = roleRepository.findByRoleName("ROLE_TEACHER")
                            .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                    break;
                default:
                    userRole = roleRepository.findByRoleName("ROLE_STUDENT")
                            .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            }
            user.setUserRole(userRole);
        }

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User updated successfully by Admin!"));
    }

    /**
     * Elimina a un usuario limpiando sus relaciones (matrículas y calificaciones).
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // 1. Limpiar matrículas
        user.getEnrolledSubjects().clear();
        userRepository.save(user);

        // 2. Limpiar calificaciones (si es estudiante o profesor)
        List<Grade> studentGrades = gradeRepository.findAll().stream()
                .filter(g -> g.getGradedStudent().getId().equals(id))
                .toList();
        gradeRepository.deleteAll(studentGrades);

        List<Grade> teacherGrades = gradeRepository.findAll().stream()
                .filter(g -> g.getGradedByTeacher().getId().equals(id))
                .toList();
        gradeRepository.deleteAll(teacherGrades);

        // 3. Limpiar asignaciones de asignaturas si es profesor
        List<Subject> subjects = subjectRepository.findAll().stream()
                .filter(s -> s.getAssignedTeacher() != null && s.getAssignedTeacher().getId().equals(id))
                .toList();
        for (Subject s : subjects) {
            s.setAssignedTeacher(null);
            subjectRepository.save(s);
        }

        // 4. Eliminar el usuario de la base de datos
        userRepository.delete(user);

        return ResponseEntity.ok(new MessageResponse("User deleted successfully by Admin!"));
    }

    /**
     * Matricula a un estudiante en una asignatura específica.
     */
    @PostMapping("/users/{studentId}/enroll/{subjectId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long studentId, @PathVariable Long subjectId) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (student.getEnrolledSubjects().contains(subject)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Student is already enrolled in this subject."));
        }

        student.getEnrolledSubjects().add(subject);
        userRepository.save(student);

        return ResponseEntity.ok(new MessageResponse("Student enrolled successfully!"));
    }

    /**
     * Elimina a un estudiante de una asignatura (desmatricular).
     */
    @DeleteMapping("/users/{studentId}/unenroll/{subjectId}")
    public ResponseEntity<?> unenrollStudent(@PathVariable Long studentId, @PathVariable Long subjectId) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        if (!student.getEnrolledSubjects().contains(subject)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Student is not enrolled in this subject."));
        }

        student.getEnrolledSubjects().remove(subject);
        userRepository.save(student);

        return ResponseEntity.ok(new MessageResponse("Student unenrolled successfully!"));
    }

    /**
     * Permite al administrador consultar las calificaciones de un alumno.
     */
    @GetMapping("/users/{studentId}/grades")
    public ResponseEntity<List<Grade>> getStudentGrades(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeRepository.findByGradedStudentId(studentId));
    }

    /**
     * Permite al administrador añadir calificaciones a un alumno.
     */
    @PostMapping("/users/{studentId}/grades")
    public ResponseEntity<?> addGrade(@PathVariable Long studentId, @RequestBody GradeRequest request) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Error: Subject not found."));

        // Obtener el administrador que realiza la acción como evaluador por defecto
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser admin = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Error: Admin user not found."));

        // Si la asignatura tiene un profesor asignado, lo usamos como evaluador principal
        AppUser evaluator = (subject.getAssignedTeacher() != null) ? subject.getAssignedTeacher() : admin;

        Grade grade = new Grade();
        grade.setGradeValue(request.getGradeValue());
        grade.setGradedStudent(student);
        grade.setEvaluatedSubject(subject);
        grade.setGradedByTeacher(evaluator);
        grade.setDateGiven(new Date());
        grade.setExamType("Admin Assign");

        gradeRepository.save(grade);
        return ResponseEntity.ok(new MessageResponse("Grade added successfully by Admin!"));
    }

    /**
     * Permite al administrador eliminar una calificación específica de un alumno.
     */
    @DeleteMapping("/users/grades/{gradeId}")
    public ResponseEntity<?> removeGrade(@PathVariable Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Error: Grade not found."));

        gradeRepository.delete(grade);
        return ResponseEntity.ok(new MessageResponse("Grade deleted successfully by Admin!"));
    }

    /**
     * Permite al administrador consultar las asignaturas matriculadas de un alumno específico.
     */
    @GetMapping("/users/{studentId}/subjects")
    public ResponseEntity<Set<Subject>> getStudentSubjects(@PathVariable Long studentId) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Error: Student not found."));
        return ResponseEntity.ok(student.getEnrolledSubjects());
    }
}