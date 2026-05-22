package pl.dmcs.david.finalproject.component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.dmcs.david.finalproject.model.AppUser;
import pl.dmcs.david.finalproject.model.Degree;
import pl.dmcs.david.finalproject.model.Grade;
import pl.dmcs.david.finalproject.model.Role;
import pl.dmcs.david.finalproject.model.Subject;
import pl.dmcs.david.finalproject.repository.AppUserRepository;
import pl.dmcs.david.finalproject.repository.DegreeRepository;
import pl.dmcs.david.finalproject.repository.GradeRepository;
import pl.dmcs.david.finalproject.repository.RoleRepository;
import pl.dmcs.david.finalproject.repository.SubjectRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Componente que se ejecuta automaticamente al iniciar la aplicacion Spring Boot.
 * Inicializa los roles, usuarios y asignaturas reales inspirados en la Universidad Tecnologica de Lodz (TUL).
 * Contiene datos masivos para una demostracion realista de la aplicacion.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository userRepository;
    private final DegreeRepository degreeRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder encoder;

    // Contador para asignar notas de forma variada
    private int gradeCounter = 0;
    private final double[] gradePool = {2.0, 3.0, 3.0, 3.5, 3.5, 4.0, 4.0, 4.5, 4.5, 5.0, 5.0, 5.0};

    public DatabaseSeeder(RoleRepository roleRepository,
                          AppUserRepository userRepository,
                          DegreeRepository degreeRepository,
                          SubjectRepository subjectRepository,
                          GradeRepository gradeRepository,
                          PasswordEncoder encoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.degreeRepository = degreeRepository;
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
        this.encoder = encoder;
    }


    @Override
    @Transactional
    public void run(String... args) throws Exception {


        /*
        // ========================================================================
        // 0. PURGADO LIMPIO DE REPOSITORIOS (Evita violaciones de integridad referencial)
        // ========================================================================
        System.out.println("⏳ Purgando base de datos antes de sembrar...");
        gradeRepository.deleteAll();

        // Limpiar relaciones many-to-many en usuarios y asignaturas antes de borrar
        for (AppUser user : userRepository.findAll()) {
            user.getEnrolledSubjects().clear();
            userRepository.save(user);
        }
        for (Subject subject : subjectRepository.findAll()) {
            subject.setAssignedTeacher(null);
            subject.getDegrees().clear();
            subjectRepository.save(subject);
        }

        userRepository.deleteAll();
        subjectRepository.deleteAll();
        degreeRepository.deleteAll();
        roleRepository.deleteAll();
        System.out.println("✅ Purga completa.");

        // ========================================================================
        // 1. INICIALIZACION DE ROLES
        // ========================================================================
        Role studentRole = getOrCreateRole("ROLE_STUDENT");
        Role teacherRole = getOrCreateRole("ROLE_TEACHER");
        Role adminRole = getOrCreateRole("ROLE_ADMIN");

        // ========================================================================
        // 2. ADMINISTRADORES DE SISTEMA (Múltiples con email y teléfono)
        // ========================================================================
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setAcademicEmail("admin@tul.edu.pl");
            admin.setPhoneNumber("123456789");
            admin.setPassword(encoder.encode("admin123"));
            admin.setUserRole(adminRole);
            userRepository.save(admin);
            System.out.println("✅ Admin 1 creado! Username: admin | Password: admin123");
        }

        if (!userRepository.existsByUsername("admin2")) {
            AppUser admin2 = new AppUser();
            admin2.setUsername("admin2");
            admin2.setFirstName("David");
            admin2.setLastName("Sanchez");
            admin2.setAcademicEmail("david.sanchez@admin.iwa.com");
            admin2.setPhoneNumber("987654321");
            admin2.setPassword(encoder.encode("admin123"));
            admin2.setUserRole(adminRole);
            userRepository.save(admin2);
            System.out.println("✅ Admin 2 creado! Username: admin2 | Password: admin123");
        }

        if (!userRepository.existsByUsername("admin3")) {
            AppUser admin3 = new AppUser();
            admin3.setUsername("admin3");
            admin3.setFirstName("Anna");
            admin3.setLastName("Kowalska");
            admin3.setAcademicEmail("anna.kowalska@admin.iwa.com");
            admin3.setPhoneNumber("555666777");
            admin3.setPassword(encoder.encode("admin123"));
            admin3.setUserRole(adminRole);
            userRepository.save(admin3);
            System.out.println("✅ Admin 3 creado! Username: admin3 | Password: admin123");
        }

        // ========================================================================
        // 3. CARRERAS UNIVERSITARIAS (8 programas reales de TUL)
        // ========================================================================
        Degree compSci = getOrCreateDegree("Computer Science", "FTIMS", 210, 3);
        Degree biomedEng = getOrCreateDegree("Biomedical Engineering", "EEIA", 210, 3);
        Degree businessStudies = getOrCreateDegree("Business Studies", "OiZ", 180, 3);
        Degree indBiotech = getOrCreateDegree("Industrial Biotechnology", "BiNoZ", 210, 3);
        Degree civilEng = getOrCreateDegree("Civil Engineering", "BAIS", 210, 3);
        Degree mechEng = getOrCreateDegree("Mechanical Engineering", "MiPKM", 210, 3);
        Degree elecEng = getOrCreateDegree("Electrical Engineering", "EEIA", 210, 3);
        Degree architecture = getOrCreateDegree("Architecture", "BAIS", 210, 4);

        // ========================================================================
        // 4. PROFESORES (Asociados obligatoriamente a una carrera)
        // ========================================================================
        AppUser profKowalski = getOrCreateTeacher("prof_kowalski", "Jan", "Kowalski", "jan.kowalski@teacher.iwa.com", compSci, teacherRole);
        AppUser profNowak = getOrCreateTeacher("prof_nowak", "Anna", "Nowak", "anna.nowak@teacher.iwa.com", compSci, teacherRole);
        AppUser profWisniewski = getOrCreateTeacher("prof_wisniewski", "Piotr", "Wisniewski", "piotr.wisniewski@teacher.iwa.com", elecEng, teacherRole);
        AppUser profWojcik = getOrCreateTeacher("prof_wojcik", "Maria", "Wojcik", "maria.wojcik@teacher.iwa.com", biomedEng, teacherRole);
        AppUser profLewandowski = getOrCreateTeacher("prof_lewandowski", "Tomasz", "Lewandowski", "tomasz.lewandowski@teacher.iwa.com", civilEng, teacherRole);
        AppUser profKaminska = getOrCreateTeacher("prof_kaminska", "Katarzyna", "Kaminska", "katarzyna.kaminska@teacher.iwa.com", businessStudies, teacherRole);
        AppUser profZielinski = getOrCreateTeacher("prof_zielinski", "Andrzej", "Zielinski", "andrzej.zielinski@teacher.iwa.com", mechEng, teacherRole);
        AppUser profSzymanska = getOrCreateTeacher("prof_szymanska", "Monika", "Szymanska", "monika.szymanska@teacher.iwa.com", indBiotech, teacherRole);
        AppUser profDabrowska = getOrCreateTeacher("prof_dabrowska", "Joanna", "Dabrowska", "joanna.dabrowska@teacher.iwa.com", architecture, teacherRole);
        AppUser profJankowski = getOrCreateTeacher("prof_jankowski", "Krzysztof", "Jankowski", "krzysztof.jankowski@teacher.iwa.com", architecture, teacherRole);

        // ========================================================================
        // 5. ESTUDIANTES (Asociados obligatoriamente a una carrera)
        // ========================================================================
        // Computer Science
        AppUser sMalinowski = getOrCreateStudent("student_malinowski", "Adam", "Malinowski", "adam.malinowski@student.iwa.com", compSci, studentRole);
        AppUser sZielinska = getOrCreateStudent("student_zielinska", "Ewa", "Zielinska", "ewa.zielinska@student.iwa.com", compSci, studentRole);
        AppUser sBorkowski = getOrCreateStudent("student_borkowski", "Jakub", "Borkowski", "jakub.borkowski@student.iwa.com", compSci, studentRole);
        AppUser sPawlak = getOrCreateStudent("student_pawlak", "Monika", "Pawlak", "monika.pawlak@student.iwa.com", compSci, studentRole);

        // Biomedical Engineering
        AppUser sSzymanski = getOrCreateStudent("student_szymanski", "Tomasz", "Szymanski", "tomasz.szymanski@student.iwa.com", biomedEng, studentRole);
        AppUser sWozniak = getOrCreateStudent("student_wozniak", "Natalia", "Wozniak", "natalia.wozniak@student.iwa.com", biomedEng, studentRole);
        AppUser sPietrzak = getOrCreateStudent("student_pietrzak", "Rafal", "Pietrzak", "rafal.pietrzak@student.iwa.com", biomedEng, studentRole);

        // Business Studies
        AppUser sDudek = getOrCreateStudent("student_dudek", "Kamil", "Dudek", "kamil.dudek@student.iwa.com", businessStudies, studentRole);
        AppUser sKaczmarek = getOrCreateStudent("student_kaczmarek", "Karolina", "Kaczmarek", "karolina.kaczmarek@student.iwa.com", businessStudies, studentRole);
        AppUser sGrabowski = getOrCreateStudent("student_grabowski", "Lukasz", "Grabowski", "lukasz.grabowski@student.iwa.com", businessStudies, studentRole);
        AppUser sKowalczyk = getOrCreateStudent("student_kowalczyk", "Marta", "Kowalczyk", "marta.kowalczyk@student.iwa.com", businessStudies, studentRole);

        // Industrial Biotechnology
        AppUser sMazur = getOrCreateStudent("student_mazur", "Mateusz", "Mazur", "mateusz.mazur@student.iwa.com", indBiotech, studentRole);
        AppUser sKrawczyk = getOrCreateStudent("student_krawczyk", "Aleksandra", "Krawczyk", "aleksandra.krawczyk@student.iwa.com", indBiotech, studentRole);
        AppUser sAdamczyk = getOrCreateStudent("student_adamczyk", "Bartosz", "Adamczyk", "bartosz.adamczyk@student.iwa.com", indBiotech, studentRole);

        // Civil Engineering
        AppUser sWalczak = getOrCreateStudent("student_walczak", "Dawid", "Walczak", "dawid.walczak@student.iwa.com", civilEng, studentRole);
        AppUser sStepien = getOrCreateStudent("student_stepien", "Agnieszka", "Stepien", "agnieszka.stepien@student.iwa.com", civilEng, studentRole);
        AppUser sJasinska = getOrCreateStudent("student_jasinska", "Patrycja", "Jasinska", "patrycja.jasinska@student.iwa.com", civilEng, studentRole);

        // Mechanical Engineering
        AppUser sSikora = getOrCreateStudent("student_sikora", "Marcin", "Sikora", "marcin.sikora@student.iwa.com", mechEng, studentRole);
        AppUser sBaran = getOrCreateStudent("student_baran", "Dominika", "Baran", "dominika.baran@student.iwa.com", mechEng, studentRole);
        AppUser sZawadzki = getOrCreateStudent("student_zawadzki", "Sebastian", "Zawadzki", "sebastian.zawadzki@student.iwa.com", mechEng, studentRole);
        AppUser sWieczorek = getOrCreateStudent("student_wieczorek", "Anna", "Wieczorek", "anna.wieczorek@student.iwa.com", mechEng, studentRole);

        // Electrical Engineering
        AppUser sTomczak = getOrCreateStudent("student_tomczak", "Filip", "Tomczak", "filip.tomczak@student.iwa.com", elecEng, studentRole);
        AppUser sChmielewski = getOrCreateStudent("student_chmielewski", "Natalia", "Chmielewski", "natalia.chmielewski@student.iwa.com", elecEng, studentRole);
        AppUser sGorski = getOrCreateStudent("student_gorski", "Piotr", "Gorski", "piotr.gorski@student.iwa.com", elecEng, studentRole);

        // Architecture
        AppUser sMichalska = getOrCreateStudent("student_michalska", "Julia", "Michalska", "julia.michalska@student.iwa.com", architecture, studentRole);
        AppUser sKrol = getOrCreateStudent("student_krol", "Igor", "Krol", "igor.krol@student.iwa.com", architecture, studentRole);
        AppUser sKozlowska = getOrCreateStudent("student_kozlowska", "Weronika", "Kozlowska", "weronika.kozlowska@student.iwa.com", architecture, studentRole);
        AppUser sSadowski = getOrCreateStudent("student_sadowski", "Krzysztof", "Sadowski", "krzysztof.sadowski@student.iwa.com", architecture, studentRole);
        AppUser sKubiak = getOrCreateStudent("student_kubiak", "Magdalena", "Kubiak", "magdalena.kubiak@student.iwa.com", architecture, studentRole);

        // ========================================================================
        // 6. ASIGNATURAS (Las asignaturas se asocian a las carreras de sus profesores correspondientes)
        // ========================================================================
        // Computer Science
        Subject cs1 = getOrCreateSubject("Database Systems", compSci, profKowalski);
        Subject cs2 = getOrCreateSubject("Software Engineering", compSci, profNowak);
        Subject cs3 = getOrCreateSubject("Artificial Intelligence", compSci, profLewandowski);
        Subject cs4 = getOrCreateSubject("Web Applications Development", compSci, profKowalski);
        Subject cs5 = getOrCreateSubject("Computer Networks", compSci, profNowak);
        Subject cs6 = getOrCreateSubject("Operating Systems", compSci, null);
        Subject cs7 = getOrCreateSubject("Algorithms and Data Structures", compSci, profNowak);

        // Biomedical Engineering
        Subject bm1 = getOrCreateSubject("Biomaterials", biomedEng, profWojcik);
        Subject bm2 = getOrCreateSubject("Medical Imaging Systems", biomedEng, profWojcik);
        Subject bm3 = getOrCreateSubject("Introduction to Bioinformatics", biomedEng, profWojcik);
        Subject bm4 = getOrCreateSubject("Biomedical Electronics", biomedEng, profWojcik);
        Subject bm5 = getOrCreateSubject("Human Anatomy", biomedEng, null);

        // Business Studies
        Subject bs1 = getOrCreateSubject("Microeconomics", businessStudies, profKaminska);
        Subject bs2 = getOrCreateSubject("Strategic Management", businessStudies, profKaminska);
        Subject bs3 = getOrCreateSubject("International Finance", businessStudies, profKaminska);
        Subject bs4 = getOrCreateSubject("Marketing", businessStudies, profKaminska);
        Subject bs5 = getOrCreateSubject("Accounting", businessStudies, null);
        Subject bs6 = getOrCreateSubject("Business Law", businessStudies, profKaminska);

        // Industrial Biotechnology
        Subject ib1 = getOrCreateSubject("General Microbiology", indBiotech, profSzymanska);
        Subject ib2 = getOrCreateSubject("Bioprocess Engineering", indBiotech, profSzymanska);
        Subject ib3 = getOrCreateSubject("Molecular Biology", indBiotech, profSzymanska);
        Subject ib4 = getOrCreateSubject("Biochemistry", indBiotech, null);
        Subject ib5 = getOrCreateSubject("Food Technology", indBiotech, profSzymanska);

        // Civil Engineering
        Subject ce1 = getOrCreateSubject("Structural Mechanics", civilEng, profLewandowski);
        Subject ce2 = getOrCreateSubject("Construction Materials", civilEng, profLewandowski);
        Subject ce3 = getOrCreateSubject("Geotechnics", civilEng, profLewandowski);
        Subject ce4 = getOrCreateSubject("Building Physics", civilEng, null);
        Subject ce5 = getOrCreateSubject("Urban Planning", civilEng, profLewandowski);

        // Mechanical Engineering
        Subject me1 = getOrCreateSubject("Thermodynamics", mechEng, profZielinski);
        Subject me2 = getOrCreateSubject("Fluid Mechanics", mechEng, profZielinski);
        Subject me3 = getOrCreateSubject("Machine Design", mechEng, profZielinski);
        Subject me4 = getOrCreateSubject("Manufacturing Technology", mechEng, null);
        Subject me5 = getOrCreateSubject("CAD Systems", mechEng, profZielinski);

        // Electrical Engineering
        Subject ee1 = getOrCreateSubject("Circuit Theory", elecEng, profWisniewski);
        Subject ee2 = getOrCreateSubject("Power Electronics", elecEng, profWisniewski);
        Subject ee3 = getOrCreateSubject("Signal Processing", elecEng, profWisniewski);
        Subject ee4 = getOrCreateSubject("Control Systems", elecEng, null);
        Subject ee5 = getOrCreateSubject("Electromagnetic Fields", elecEng, profWisniewski);

        // Architecture
        Subject ar1 = getOrCreateSubject("Architectural Design", architecture, profJankowski);
        Subject ar2 = getOrCreateSubject("History of Architecture", architecture, profDabrowska);
        Subject ar3 = getOrCreateSubject("Building Structures", architecture, profJankowski);
        Subject ar4 = getOrCreateSubject("Landscape Architecture", architecture, null);
        Subject ar5 = getOrCreateSubject("Interior Design", architecture, profDabrowska);

        // ========================================================================
        // 7. MATRICULACIONES (Estudiantes matriculados SOLAMENTE en asignaturas de su carrera)
        // ========================================================================
        // Computer Science
        enrollStudent(sMalinowski, cs1, cs2, cs3, cs5);
        enrollStudent(sZielinska, cs1, cs4, cs7, cs3);
        enrollStudent(sBorkowski, cs2, cs5, cs6, cs7);
        enrollStudent(sPawlak, cs1, cs3, cs4);

        // Biomedical Engineering
        enrollStudent(sSzymanski, bm1, bm2, bm3);
        enrollStudent(sWozniak, bm1, bm4, bm3, bm5);
        enrollStudent(sPietrzak, bm2, bm3, bm4);

        // Business Studies
        enrollStudent(sDudek, bs1, bs2, bs3, bs6);
        enrollStudent(sKaczmarek, bs1, bs4, bs5, bs2);
        enrollStudent(sGrabowski, bs2, bs3, bs6);
        enrollStudent(sKowalczyk, bs1, bs3, bs4, bs6);

        // Industrial Biotechnology
        enrollStudent(sMazur, ib1, ib2, ib3);
        enrollStudent(sKrawczyk, ib1, ib3, ib5);
        enrollStudent(sAdamczyk, ib2, ib4, ib5);

        // Civil Engineering
        enrollStudent(sWalczak, ce1, ce2, ce3, ce5);
        enrollStudent(sStepien, ce1, ce3, ce4);
        enrollStudent(sJasinska, ce2, ce4, ce5);

        // Mechanical Engineering
        enrollStudent(sSikora, me1, me2, me3, me5);
        enrollStudent(sBaran, me1, me3, me4);
        enrollStudent(sZawadzki, me2, me4, me5);
        enrollStudent(sWieczorek, me1, me2, me5);

        // Electrical Engineering
        enrollStudent(sTomczak, ee1, ee2, ee3);
        enrollStudent(sChmielewski, ee1, ee3, ee5);
        enrollStudent(sGorski, ee2, ee4, ee5);

        // Architecture
        enrollStudent(sMichalska, ar1, ar2, ar3, ar5);
        enrollStudent(sKrol, ar1, ar3, ar4);
        enrollStudent(sKozlowska, ar2, ar3, ar5);
        enrollStudent(sSadowski, ar1, ar4, ar5);
        enrollStudent(sKubiak, ar1, ar2, ar3);

        // ========================================================================
        // 8. CALIFICACIONES (notas finales para ~65% de las matriculaciones con profesor)
        // ========================================================================
        // Computer Science
        assignGradeIfTeacher(sMalinowski, cs1); assignGradeIfTeacher(sMalinowski, cs2); assignGradeIfTeacher(sMalinowski, cs3);
        assignGradeIfTeacher(sZielinska, cs1); assignGradeIfTeacher(sZielinska, cs4);
        assignGradeIfTeacher(sBorkowski, cs2); assignGradeIfTeacher(sBorkowski, cs5); assignGradeIfTeacher(sBorkowski, cs7);
        assignGradeIfTeacher(sPawlak, cs1); assignGradeIfTeacher(sPawlak, cs3);

        // Biomedical Engineering
        assignGradeIfTeacher(sSzymanski, bm1); assignGradeIfTeacher(sSzymanski, bm2);
        assignGradeIfTeacher(sWozniak, bm1); assignGradeIfTeacher(sWozniak, bm4); assignGradeIfTeacher(sWozniak, bm3);
        assignGradeIfTeacher(sPietrzak, bm2); assignGradeIfTeacher(sPietrzak, bm3);

        // Business Studies
        assignGradeIfTeacher(sDudek, bs1); assignGradeIfTeacher(sDudek, bs2); assignGradeIfTeacher(sDudek, bs3);
        assignGradeIfTeacher(sKaczmarek, bs1); assignGradeIfTeacher(sKaczmarek, bs4);
        assignGradeIfTeacher(sGrabowski, bs2); assignGradeIfTeacher(sGrabowski, bs3);
        assignGradeIfTeacher(sKowalczyk, bs1); assignGradeIfTeacher(sKowalczyk, bs3); assignGradeIfTeacher(sKowalczyk, bs6);

        // Industrial Biotechnology
        assignGradeIfTeacher(sMazur, ib1); assignGradeIfTeacher(sMazur, ib2);
        assignGradeIfTeacher(sKrawczyk, ib1); assignGradeIfTeacher(sKrawczyk, ib5);
        assignGradeIfTeacher(sAdamczyk, ib2); assignGradeIfTeacher(sAdamczyk, ib5);

        // Civil Engineering
        assignGradeIfTeacher(sWalczak, ce1); assignGradeIfTeacher(sWalczak, ce2); assignGradeIfTeacher(sWalczak, ce3);
        assignGradeIfTeacher(sStepien, ce1); assignGradeIfTeacher(sStepien, ce3);
        assignGradeIfTeacher(sJasinska, ce2); assignGradeIfTeacher(sJasinska, ce5);

        // Mechanical Engineering
        assignGradeIfTeacher(sSikora, me1); assignGradeIfTeacher(sSikora, me2); assignGradeIfTeacher(sSikora, me5);
        assignGradeIfTeacher(sBaran, me1); assignGradeIfTeacher(sBaran, me3);
        assignGradeIfTeacher(sZawadzki, me2); assignGradeIfTeacher(sZawadzki, me5);
        assignGradeIfTeacher(sWieczorek, me1); assignGradeIfTeacher(sWieczorek, me2);

        // Electrical Engineering
        assignGradeIfTeacher(sTomczak, ee1); assignGradeIfTeacher(sTomczak, ee2);
        assignGradeIfTeacher(sChmielewski, ee1); assignGradeIfTeacher(sChmielewski, ee3);
        assignGradeIfTeacher(sGorski, ee2); assignGradeIfTeacher(sGorski, ee5);

        // Architecture
        assignGradeIfTeacher(sMichalska, ar1); assignGradeIfTeacher(sMichalska, ar2); assignGradeIfTeacher(sMichalska, ar5);
        assignGradeIfTeacher(sKrol, ar1); assignGradeIfTeacher(sKrol, ar3);
        assignGradeIfTeacher(sKozlowska, ar2); assignGradeIfTeacher(sKozlowska, ar3);
        assignGradeIfTeacher(sSadowski, ar1); assignGradeIfTeacher(sSadowski, ar5);
        assignGradeIfTeacher(sKubiak, ar1); assignGradeIfTeacher(sKubiak, ar2); assignGradeIfTeacher(sKubiak, ar3);

        System.out.println("✅ Base de datos sembrada con éxito.");
        */
    }



    // ============================================================================
    // MÉTODOS AUXILIARES
    // ============================================================================

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private Degree getOrCreateDegree(String name, String faculty, int credits, int duration) {
        return degreeRepository.findByName(name)
                .orElseGet(() -> {
                    Degree d = new Degree();
                    d.setName(name);
                    d.setFaculty(faculty);
                    d.setTotalCredits(credits);
                    d.setDurationYears(duration);
                    return degreeRepository.save(d);
                });
    }

    private AppUser getOrCreateTeacher(String username, String firstName, String lastName, String email, Degree degree, Role role) {
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        AppUser teacher = new AppUser();
        teacher.setUsername(username);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setAcademicEmail(email);
        teacher.setPassword(encoder.encode("password123"));
        teacher.setUserRole(role);
        teacher.setDegree(degree);
        return userRepository.save(teacher);
    }

    private AppUser getOrCreateStudent(String username, String firstName, String lastName, String email, Degree degree, Role role) {
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        AppUser student = new AppUser();
        student.setUsername(username);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setAcademicEmail(email);
        student.setPassword(encoder.encode("password123"));
        student.setUserRole(role);
        student.setDegree(degree);
        return userRepository.save(student);
    }

    private Subject getOrCreateSubject(String name, Degree degree, AppUser teacher) {
        // Buscamos si la asignatura ya existe por nombre
        Optional<Subject> subjectOpt = subjectRepository.findAll().stream()
                .filter(sub -> sub.getSubjectName().equalsIgnoreCase(name))
                .findFirst();
        if (subjectOpt.isPresent()) {
            Subject subject = subjectOpt.get();
            // Asegurar que esta asociada a la carrera correspondiente
            if (!subject.getDegrees().contains(degree)) {
                subject.getDegrees().add(degree);
            }
            if (teacher != null && teacher.getDegree() != null && !subject.getDegrees().contains(teacher.getDegree())) {
                subject.getDegrees().add(teacher.getDegree());
            }
            subjectRepository.save(subject);
            return subject;
        } else {
            Subject subject = new Subject();
            subject.setSubjectName(name);
            subject.getDegrees().add(degree);
            if (teacher != null) {
                subject.setAssignedTeacher(teacher);
                if (teacher.getDegree() != null) {
                    subject.getDegrees().add(teacher.getDegree());
                }
            }
            subject.setEnrolledStudents(new HashSet<>());
            return subjectRepository.save(subject);
        }
    }

    // Matricula a un estudiante en varias asignaturas
    private void enrollStudent(AppUser student, Subject... subjects) {
        for (Subject subject : subjects) {
            if (!student.getEnrolledSubjects().contains(subject)) {
                student.getEnrolledSubjects().add(subject);
            }
        }
        userRepository.save(student);
    }

    // Asigna una calificacion solo si la asignatura tiene profesor asignado
    private void assignGradeIfTeacher(AppUser student, Subject subject) {
        if (subject.getAssignedTeacher() == null) {
            return; // Sin profesor, no se puede calificar
        }

        // Verificar que no exista ya una calificacion para este estudiante en esta asignatura
        boolean alreadyGraded = gradeRepository.findAll().stream()
                .anyMatch(g -> g.getGradedStudent().getId().equals(student.getId()) &&
                               g.getEvaluatedSubject().getId().equals(subject.getId()));
        if (alreadyGraded) {
            return;
        }

        Grade grade = new Grade();
        grade.setGradeValue(getNextGrade());
        grade.setGradedStudent(student);
        grade.setEvaluatedSubject(subject);
        grade.setGradedByTeacher(subject.getAssignedTeacher());
        grade.setDateGiven(new Date());
        grade.setExamType("Final Exam");
        gradeRepository.save(grade);
    }

    // Devuelve notas variadas recorriendo el pool ciclicamente
    private double getNextGrade() {
        double grade = gradePool[gradeCounter % gradePool.length];
        gradeCounter++;
        return grade;
    }
}