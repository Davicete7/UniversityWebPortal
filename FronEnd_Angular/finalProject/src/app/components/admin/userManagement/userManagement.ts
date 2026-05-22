import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../services/admin/admin';
import { SubjectService, Subject } from '../../../services/subject/subject';
import { SignupRequest } from '../../../models/adminModel';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './userManagement.html',
  styleUrl: './userManagement.css',
})
export class UserManagementComponent implements OnInit {
  // Pestaña activa por defecto
  activeTab: 'create' | 'directory' | 'degrees' = 'directory';

  // --- MODELO PARA CREAR NUEVO USUARIO ---
  newUser: SignupRequest = {
    username: '',
    firstName: '',
    lastName: '',
    academicEmail: '',
    password: '',
    role: 'ROLE_STUDENT',
    degreeName: '',
  };

  // --- LISTAS DE DATOS ---
  usersList: any[] = [];
  degreesList: any[] = [];
  subjectsList: any[] = [];

  // --- BUSQUEDA Y FILTRADO ---
  searchQuery: string = '';

  // --- SELECCION Y EDICION DE USUARIO ---
  selectedUser: any = null;
  selectedUserSubjects: any[] = [];
  selectedUserGrades: any[] = [];

  // Formulario para matricular o añadir calificaciones a un alumno seleccionado
  newEnrollmentSubjectId: number = 0;
  newGrade: { subjectId: number; gradeValue: number } = {
    subjectId: 0,
    gradeValue: 5.0,
  };

  // --- GESTION DE CARRERAS (DEGREES) ---
  newDegree: { name: string; faculty: string; totalCredits: number; durationYears: number } = {
    name: '',
    faculty: '',
    totalCredits: 180,
    durationYears: 3,
  };
  editingDegree: any = null; // Guarda la carrera que se esta editando actualmente
  degreeAssign: { userId: number; degreeId: number } = {
    userId: 0,
    degreeId: 0,
  };

  // --- VARIABLES PARA LA ORDENACION ---
  usersSortColumn: string = '';
  usersSortAscending: boolean = true;

  gradesSortColumn: string = '';
  gradesSortAscending: boolean = true;

  degreesSortColumn: string = '';
  degreesSortAscending: boolean = true;

  // --- MENSAJES DE NOTIFICACION ---
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private adminService: AdminService,
    private subjectService: SubjectService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Leer la pestaña activa de los datos de la ruta
    const tabFromRoute = this.route.snapshot.data['tab'];
    if (tabFromRoute) {
      this.activeTab = tabFromRoute;
    }
    this.loadAllData();
  }

  // Carga todos los datos necesarios para la gestión
  loadAllData(): void {
    this.loadUsers();
    this.loadDegrees();
    this.loadSubjects();
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.usersList = data;
        this.applyUsersSort();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading users:', err);
      }
    });
  }

  loadDegrees(): void {
    this.adminService.getAllDegrees().subscribe({
      next: (data) => {
        this.degreesList = data;
        this.applyDegreesSort();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading degrees:', err);
      }
    });
  }

  loadSubjects(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        this.subjectsList = data;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading subjects:', err);
      }
    });
  }

  // Filtra los usuarios en base a la barra de búsqueda (por username, email o rol)
  get filteredUsers(): any[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.usersList;
    return this.usersList.filter(u => 
      u.username?.toLowerCase().includes(q) ||
      u.academicEmail?.toLowerCase().includes(q) ||
      u.firstName?.toLowerCase().includes(q) ||
      u.lastName?.toLowerCase().includes(q) ||
      u.userRole?.roleName?.toLowerCase().includes(q)
    );
  }

  // Selecciona un usuario para ver detalles y gestionar (perfil, asignaturas, notas)
  selectUser(user: any): void {
    this.successMessage = '';
    this.errorMessage = '';
    
    // Clonamos el objeto para evitar mutación directa
    this.selectedUser = { 
      ...user, 
      password: '', // Dejamos el password en blanco por defecto (opcional al editar)
      role: user.userRole?.roleName || 'ROLE_STUDENT',
      degreeId: user.degree?.id || null
    };

    // Si es estudiante, cargamos sus asignaturas y notas
    if (this.selectedUser.role === 'ROLE_STUDENT') {
      this.loadStudentEnrollmentsAndGrades(user.id);
    } else {
      this.selectedUserSubjects = [];
      this.selectedUserGrades = [];
    }

    this.cdr.markForCheck();
  }

  loadStudentEnrollmentsAndGrades(studentId: number): void {
    // Cargar asignaturas matriculadas
    this.adminService.getStudentSubjects(studentId).subscribe({
      next: (subjects) => {
        this.selectedUserSubjects = subjects;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading student subjects:', err);
      }
    });

    // Cargar notas
    this.adminService.getStudentGrades(studentId).subscribe({
      next: (grades) => {
        this.selectedUserGrades = grades;
        this.applyGradesSort();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading student grades:', err);
      }
    });
  }

  // Cierra el panel de edición
  closeEditPanel(): void {
    this.selectedUser = null;
    this.selectedUserSubjects = [];
    this.selectedUserGrades = [];
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.markForCheck();
  }

  // Cambia de pestaña principal
  switchTab(tab: 'create' | 'directory' | 'degrees'): void {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
    this.selectedUser = null;
    this.loadAllData();
    this.cdr.markForCheck();
  }

  // --- ACCIONES DE DIRECCIONARIO (TIPO CRUD) ---

  // Actualiza los datos del perfil seleccionado
  onUpdateUser(): void {
    this.successMessage = '';
    this.errorMessage = '';

    const missingFields: string[] = [];
    if (!this.selectedUser.username?.trim()) missingFields.push('Username');
    if (!this.selectedUser.firstName?.trim()) missingFields.push('First Name');
    if (!this.selectedUser.lastName?.trim()) missingFields.push('Last Name');
    if (!this.selectedUser.academicEmail?.trim()) missingFields.push('Academic Email');

    const isStudentOrTeacher = this.selectedUser.role === 'ROLE_STUDENT' || this.selectedUser.role === 'ROLE_TEACHER' || this.selectedUser.role === 'student' || this.selectedUser.role === 'teacher';
    if (isStudentOrTeacher && !this.selectedUser.degreeId) {
      missingFields.push('Academic Degree');
    }

    if (missingFields.length > 0) {
      this.errorMessage = `Unfilled fields: ${missingFields.join(', ')}`;
      this.cdr.markForCheck();
      return;
    }

    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(this.selectedUser.academicEmail)) {
      this.errorMessage = 'Invalid email format. Please enter a valid email address.';
      this.cdr.markForCheck();
      return;
    }

    this.adminService.updateUser(this.selectedUser.id, this.selectedUser).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'User updated successfully!';
        this.loadUsers();
        // Volvemos a seleccionar el usuario actualizado para refrescar los datos
        const updated = this.usersList.find(u => u.id === this.selectedUser.id);
        if (updated) {
          this.selectUser(updated);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error updating user.';
        this.cdr.markForCheck();
      }
    });
  }

  // Elimina un usuario por completo
  onDeleteUser(userId: number): void {
    if (!confirm('Are you sure you want to delete this user profile? This action is irreversible.')) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.deleteUser(userId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'User deleted successfully!';
        this.selectedUser = null;
        this.loadUsers();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error deleting user.';
        this.cdr.markForCheck();
      }
    });
  }

  // --- GESTIÓN DE MATRÍCULAS ---

  onEnrollStudent(): void {
    if (!this.newEnrollmentSubjectId) {
      this.errorMessage = 'Please select a subject to enroll.';
      this.cdr.markForCheck();
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.enrollStudent(this.selectedUser.id, this.newEnrollmentSubjectId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Student enrolled successfully!';
        this.loadStudentEnrollmentsAndGrades(this.selectedUser.id);
        this.newEnrollmentSubjectId = 0;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error enrolling student.';
        this.cdr.markForCheck();
      }
    });
  }

  onUnenrollStudent(subjectId: number): void {
    if (!confirm('Are you sure you want to unenroll the student from this subject?')) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.unenrollStudent(this.selectedUser.id, subjectId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Student unenrolled successfully!';
        this.loadStudentEnrollmentsAndGrades(this.selectedUser.id);
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error unenrolling student.';
        this.cdr.markForCheck();
      }
    });
  }

  // --- GESTIÓN DE NOTAS (GRADES) ---

  onAddGrade(): void {
    if (!this.newGrade.subjectId) {
      this.errorMessage = 'Please select a subject for the grade.';
      this.cdr.markForCheck();
      return;
    }

    if (this.newGrade.gradeValue < 2.0 || this.newGrade.gradeValue > 5.5) {
      this.errorMessage = 'Grade must be between 2.0 and 5.5.';
      this.cdr.markForCheck();
      return;
    }

    // Regla de negocio: Comprobar si el alumno ya posee una calificación para esa asignatura
    const hasGrade = this.selectedUserGrades.some(g => g.evaluatedSubject?.id === Number(this.newGrade.subjectId));
    if (hasGrade) {
      this.errorMessage = 'Student already has a final grade for this subject.';
      this.cdr.markForCheck();
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.addGrade(this.selectedUser.id, this.newGrade).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Grade assigned successfully!';
        this.loadStudentEnrollmentsAndGrades(this.selectedUser.id);
        this.newGrade = { subjectId: 0, gradeValue: 5.0 };
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error assigning grade.';
        this.cdr.markForCheck();
      }
    });
  }

  onDeleteGrade(gradeId: number): void {
    if (!confirm('Are you sure you want to delete this grade?')) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.deleteGrade(gradeId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Grade deleted successfully!';
        this.loadStudentEnrollmentsAndGrades(this.selectedUser.id);
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error deleting grade.';
        this.cdr.markForCheck();
      }
    });
  }

  // --- CREAR NUEVO USUARIO (TAB 1) ---

  onSubmit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    const missingFields: string[] = [];
    if (!this.newUser.username?.trim()) missingFields.push('Username');
    if (!this.newUser.password?.trim()) missingFields.push('Temporary Password');
    if (!this.newUser.firstName?.trim()) missingFields.push('First Name');
    if (!this.newUser.lastName?.trim()) missingFields.push('Last Name');
    if (!this.newUser.academicEmail?.trim()) missingFields.push('Academic Email');
    if (!this.newUser.role?.trim()) missingFields.push('System Role');
    
    if ((this.newUser.role === 'ROLE_STUDENT' || this.newUser.role === 'ROLE_TEACHER') && !this.newUser.degreeName?.trim()) {
      missingFields.push('Degree Program');
    }

    if (missingFields.length > 0) {
      this.errorMessage = `Unfilled fields: ${missingFields.join(', ')}`;
      this.cdr.markForCheck();
      return;
    }

    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(this.newUser.academicEmail)) {
      this.errorMessage = 'Invalid email format. Please enter a valid email address.';
      this.cdr.markForCheck();
      return;
    }

    this.adminService.createUser(this.newUser).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'User created successfully!';
        this.newUser = {
          username: '',
          firstName: '',
          lastName: '',
          academicEmail: '',
          password: '',
          role: 'ROLE_STUDENT',
          degreeName: '',
        };
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error creating user.';
        this.cdr.markForCheck();
      },
    });
  }

  // --- GESTIÓN DE CARRERAS (TAB 3) ---

  onCreateDegree(): void {
    this.successMessage = '';
    this.errorMessage = '';

    const missingFields: string[] = [];
    if (!this.newDegree.name?.trim()) missingFields.push('Degree Name');
    if (!this.newDegree.faculty?.trim()) missingFields.push('Faculty/Department');
    if (!this.newDegree.totalCredits) missingFields.push('Total ECTS Credits');
    if (!this.newDegree.durationYears) missingFields.push('Duration (Years)');

    if (missingFields.length > 0) {
      this.errorMessage = `Unfilled fields: ${missingFields.join(', ')}`;
      this.cdr.markForCheck();
      return;
    }

    this.adminService.createDegree(this.newDegree).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Degree program created successfully!';
        this.newDegree = {
          name: '',
          faculty: '',
          totalCredits: 180,
          durationYears: 3,
        };
        this.loadDegrees();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error creating degree.';
        this.cdr.markForCheck();
      }
    });
  }

  onDeleteDegree(degreeId: number): void {
    if (!confirm('Are you sure you want to delete this degree? This will remove degree associations from users and subjects.')) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.deleteDegree(degreeId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Degree deleted successfully!';
        this.loadDegrees();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error deleting degree.';
        this.cdr.markForCheck();
      }
    });
  }

  // Asigna un usuario a una carrera universitaria
  onAssignUserToDegree(): void {
    if (!this.degreeAssign.userId || !this.degreeAssign.degreeId) {
      this.errorMessage = 'Por favor, seleccione tanto un usuario como una carrera.';
      this.cdr.markForCheck();
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.adminService.assignUserToDegree(this.degreeAssign.degreeId, this.degreeAssign.userId).subscribe({
      next: (response) => {
        this.successMessage = response.message || '¡Usuario asignado con éxito a la carrera!';
        this.degreeAssign = { userId: 0, degreeId: 0 };
        this.loadUsers();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al asignar usuario a la carrera.';
        this.cdr.markForCheck();
      }
    });
  }

  // --- MÉTODOS DE EDICIÓN DE CARRERAS (DEGREES) ---

  // Inicia la edición de una carrera seleccionada
  onEditDegree(degree: any): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.editingDegree = { ...degree };
    this.cdr.markForCheck();
  }

  // Cancela el proceso de edición de la carrera
  onCancelEditDegree(): void {
    this.editingDegree = null;
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.markForCheck();
  }

  // Envía la petición para actualizar la carrera
  onUpdateDegree(): void {
    this.successMessage = '';
    this.errorMessage = '';

    const missingFields: string[] = [];
    if (!this.editingDegree.name?.trim()) missingFields.push('Nombre de la Carrera');
    if (!this.editingDegree.faculty?.trim()) missingFields.push('Facultad/Departamento');
    if (!this.editingDegree.totalCredits) missingFields.push('Créditos ECTS Totales');
    if (!this.editingDegree.durationYears) missingFields.push('Duración (Años)');

    if (missingFields.length > 0) {
      this.errorMessage = `Campos vacíos: ${missingFields.join(', ')}`;
      this.cdr.markForCheck();
      return;
    }

    this.adminService.updateDegree(this.editingDegree.id, this.editingDegree).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Carrera universitaria actualizada con éxito.';
        this.editingDegree = null;
        this.loadDegrees();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al actualizar la carrera.';
        this.cdr.markForCheck();
      }
    });
  }

  // --- MÉTODOS DE ORDENACIÓN UNIVERSAL ---

  // Compara dos valores de forma genérica para la ordenación
  private compareValues(a: any, b: any, ascending: boolean): number {
    if (a === null || a === undefined) return 1;
    if (b === null || b === undefined) return -1;
    if (a === b) return 0;
    
    let comparison = 0;
    if (typeof a === 'string' && typeof b === 'string') {
      comparison = a.localeCompare(b);
    } else {
      comparison = a < b ? -1 : 1;
    }
    return ascending ? comparison : -comparison;
  }

  // Ordena la lista de usuarios según la columna seleccionada
  sortUsers(column: string): void {
    if (this.usersSortColumn === column) {
      this.usersSortAscending = !this.usersSortAscending;
    } else {
      this.usersSortColumn = column;
      this.usersSortAscending = true;
    }
    this.applyUsersSort();
  }

  // Aplica la ordenación a la lista de usuarios
  applyUsersSort(): void {
    if (!this.usersSortColumn) return;
    this.usersList.sort((a, b) => {
      let valA: any = null;
      let valB: any = null;

      switch (this.usersSortColumn) {
        case 'username':
          valA = a.username;
          valB = b.username;
          break;
        case 'firstName':
          valA = a.firstName;
          valB = b.firstName;
          break;
        case 'lastName':
          valA = a.lastName;
          valB = b.lastName;
          break;
        case 'academicEmail':
          valA = a.academicEmail;
          valB = b.academicEmail;
          break;
        case 'role':
          valA = a.userRole?.roleName;
          valB = b.userRole?.roleName;
          break;
        case 'degree':
          valA = a.degree?.name;
          valB = b.degree?.name;
          break;
        default:
          return 0;
      }
      return this.compareValues(valA, valB, this.usersSortAscending);
    });
  }

  // Ordena las notas del alumno seleccionado
  sortGrades(column: string): void {
    if (this.gradesSortColumn === column) {
      this.gradesSortAscending = !this.gradesSortAscending;
    } else {
      this.gradesSortColumn = column;
      this.gradesSortAscending = true;
    }
    this.applyGradesSort();
  }

  // Aplica la ordenación a las notas
  applyGradesSort(): void {
    if (!this.gradesSortColumn) return;
    this.selectedUserGrades.sort((a, b) => {
      let valA: any = null;
      let valB: any = null;

      switch (this.gradesSortColumn) {
        case 'subject':
          valA = a.evaluatedSubject?.subjectName;
          valB = b.evaluatedSubject?.subjectName;
          break;
        case 'value':
          valA = a.gradeValue;
          valB = b.gradeValue;
          break;
        case 'date':
          valA = a.dateGiven;
          valB = b.dateGiven;
          break;
        case 'examiner':
          valA = a.gradedByTeacher ? `${a.gradedByTeacher.firstName} ${a.gradedByTeacher.lastName}` : null;
          valB = b.gradedByTeacher ? `${b.gradedByTeacher.firstName} ${b.gradedByTeacher.lastName}` : null;
          break;
        default:
          return 0;
      }
      return this.compareValues(valA, valB, this.gradesSortAscending);
    });
  }

  // Ordena la lista de carreras
  sortDegrees(column: string): void {
    if (this.degreesSortColumn === column) {
      this.degreesSortAscending = !this.degreesSortAscending;
    } else {
      this.degreesSortColumn = column;
      this.degreesSortAscending = true;
    }
    this.applyDegreesSort();
  }

  // Aplica la ordenación a las carreras
  applyDegreesSort(): void {
    if (!this.degreesSortColumn) return;
    this.degreesList.sort((a, b) => {
      let valA: any = null;
      let valB: any = null;

      switch (this.degreesSortColumn) {
        case 'name':
          valA = a.name;
          valB = b.name;
          break;
        case 'faculty':
          valA = a.faculty;
          valB = b.faculty;
          break;
        case 'totalCredits':
          valA = a.totalCredits;
          valB = b.totalCredits;
          break;
        case 'durationYears':
          valA = a.durationYears;
          valB = b.durationYears;
          break;
        default:
          return 0;
      }
      return this.compareValues(valA, valB, this.degreesSortAscending);
    });
  }
}
