import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubjectService, Subject, EnrolledStudent } from '../../services/subject/subject';
import { AdminService } from '../../services/admin/admin';
import { ProfileService } from '../../services/profile/profile';

@Component({
  selector: 'app-subject-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subjectManagement.html',
  styleUrl: './subjectManagement.css',
})
export class SubjectManagementComponent implements OnInit {
  // Lista de todas las asignaturas obtenidas del backend
  subjects: Subject[] = [];

  // Lista de todas las carreras (Degrees) para el selector de creación/edición
  degreesList: any[] = [];

  // Lista de profesores para asignar a asignaturas
  teachersList: any[] = [];

  // Variables para búsqueda y ordenación interactiva
  searchQuery: string = '';
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  // --- FORMULARIO DE CREACIÓN DE Asignatura ---
  newSubjectName: string = '';
  newSubjectDegreeIds: number[] = [];
  newSubjectTeacherId: number | null = null;

  // --- FORMULARIO DE EDICIÓN DE Asignatura ---
  selectedEditSubject: Subject | null = null;
  editSubjectName: string = '';
  editSubjectDegreeIds: number[] = [];
  editSubjectTeacherId: number | null = null;

  // Variable de sesión para el rol del usuario autenticado
  userRole: string | null = '';

  // Id del usuario autenticado para comprobar asignaciones de profesores
  userId: number | null = null;

  // Lista de estudiantes matriculados en la asignatura seleccionada
  enrolledStudents: EnrolledStudent[] = [];

  // Variables para la ordenación de alumnos matriculados
  studentsSortColumn: string = '';
  studentsSortAscending: boolean = true;

  // Nombre de la asignatura que se está consultando actualmente
  selectedSubjectName: string = '';

  teacherDegree: string = '';

  // Mensajes informativos de éxito y error para la interfaz
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private subjectService: SubjectService,
    private adminService: AdminService,
    private profileService: ProfileService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    // Leemos los datos guardados en el almacenamiento del navegador tras el login
    this.userRole = localStorage.getItem('role');
    const storedId = localStorage.getItem('userId');
    if (storedId) {
      this.userId = parseInt(storedId, 10);
    }

    // Cargamos el listado inicial de asignaturas
    this.loadTeacherDegree();
    this.loadSubjects();

    // Si es administrador, cargamos también las carreras y los profesores
    if (this.userRole === 'ROLE_ADMIN') {
      this.loadDegrees();
      this.loadTeachers();
    }
  }

  // Carga el degree del teacher
  loadTeacherDegree(): void {
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.teacherDegree = profile?.degreeName;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading degre of Teacher:', err);
      },
    });
  }

  // Carga todas las carreras disponibles
  loadDegrees(): void {
    this.adminService.getAllDegrees().subscribe({
      next: (data) => {
        this.degreesList = data;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading degrees:', err);
      },
    });
  }

  // Carga todos los usuarios y los filtra para obtener solo los profesores
  loadTeachers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.teachersList = data.filter((u) => u.userRole?.roleName === 'ROLE_TEACHER');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading teachers:', err);
      },
    });
  }

  // Realiza la petición para obtener todas las asignaturas y refrescar la pantalla
  loadSubjects(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        // Aplicamos el filtro antes de guardar los datos en this.subjects
        this.subjects = data.filter((subject) =>
          subject.degrees?.some((degree) => degree.name === this.teacherDegree),
        );

        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load subjects. Please check database connection.';
        console.error('Error loading subjects:', err);
        this.cdr.markForCheck();
      },
    });
  }

  // Método para alternar la ordenación según la columna seleccionada
  toggleSort(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.cdr.markForCheck();
  }

  // Getter para procesar la ordenación de las asignaturas
  get sortedSubjects(): Subject[] {
    const sorted = [...this.subjects];
    if (!this.sortColumn) return sorted;

    return sorted.sort((a, b) => {
      let valA: any = '';
      let valB: any = '';

      if (this.sortColumn === 'id') {
        valA = a.id || 0;
        valB = b.id || 0;
      } else if (this.sortColumn === 'name') {
        valA = (a.subjectName || '').toLowerCase();
        valB = (b.subjectName || '').toLowerCase();
      } else if (this.sortColumn === 'teacher') {
        const nameA = a.assignedTeacher
          ? `${a.assignedTeacher.firstName} ${a.assignedTeacher.lastName}`
          : 'unassigned';
        const nameB = b.assignedTeacher
          ? `${b.assignedTeacher.firstName} ${b.assignedTeacher.lastName}`
          : 'unassigned';
        valA = nameA.toLowerCase();
        valB = nameB.toLowerCase();
      } else if (this.sortColumn === 'degree') {
        const degA =
          a.degrees && a.degrees.length > 0
            ? a.degrees.map((d) => d.name).join(', ')
            : 'unassigned';
        const degB =
          b.degrees && b.degrees.length > 0
            ? b.degrees.map((d) => d.name).join(', ')
            : 'unassigned';
        valA = degA.toLowerCase();
        valB = degB.toLowerCase();
      }

      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  // Getter para filtrar las asignaturas ordenadas según la búsqueda
  get filteredSubjects(): Subject[] {
    if (!this.searchQuery || !this.searchQuery.trim()) {
      return this.sortedSubjects;
    }
    const query = this.searchQuery.toLowerCase().trim();
    return this.sortedSubjects.filter((sub) => {
      const matchId = sub.id?.toString().includes(query) || false;
      const matchName = sub.subjectName?.toLowerCase().includes(query) || false;
      const teacherName = sub.assignedTeacher
        ? `${sub.assignedTeacher.firstName} ${sub.assignedTeacher.lastName}`
        : 'unassigned';
      const matchTeacher = teacherName.toLowerCase().includes(query);
      const degreeName =
        sub.degrees && sub.degrees.length > 0
          ? sub.degrees.map((d) => d.name).join(', ')
          : 'unassigned';
      const matchDegree = degreeName.toLowerCase().includes(query);
      return matchId || matchName || matchTeacher || matchDegree;
    });
  }

  // Gestión de Checkboxes de Carreras para Crear Asignatura
  onToggleNewSubjectDegree(degreeId: number, checked: boolean): void {
    if (checked) {
      if (!this.newSubjectDegreeIds.includes(degreeId)) {
        this.newSubjectDegreeIds.push(degreeId);
      }
    } else {
      this.newSubjectDegreeIds = this.newSubjectDegreeIds.filter((id) => id !== degreeId);
    }
    this.cdr.markForCheck();
  }

  // Gestión de Checkboxes de Carreras para Editar Asignatura
  onToggleEditSubjectDegree(degreeId: number, checked: boolean): void {
    if (checked) {
      if (!this.editSubjectDegreeIds.includes(degreeId)) {
        this.editSubjectDegreeIds.push(degreeId);
      }
    } else {
      this.editSubjectDegreeIds = this.editSubjectDegreeIds.filter((id) => id !== degreeId);
    }
    this.cdr.markForCheck();
  }

  isNewDegreeChecked(degreeId: number): boolean {
    return this.newSubjectDegreeIds.includes(degreeId);
  }

  isEditDegreeChecked(degreeId: number): boolean {
    return this.editSubjectDegreeIds.includes(degreeId);
  }

  // selecciona una asignatura para cargar su formulario de edición
  onSelectEditSubject(subject: Subject): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.selectedEditSubject = subject;
    this.editSubjectName = subject.subjectName;
    this.editSubjectTeacherId = subject.assignedTeacher?.id || null;
    this.editSubjectDegreeIds = subject.degrees?.map((d) => d.id) || [];
    this.cdr.markForCheck();
  }

  // Cancela la edición y cierra el panel correspondiente
  onCancelEditSubject(): void {
    this.selectedEditSubject = null;
    this.editSubjectName = '';
    this.editSubjectDegreeIds = [];
    this.editSubjectTeacherId = null;
    this.cdr.markForCheck();
  }

  // Ejecuta la validación manual y creación de una nueva asignatura (solo ROLE_ADMIN)
  onCreateSubject(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.newSubjectName?.trim()) {
      this.errorMessage = 'Unfilled fields: Subject Name';
      this.cdr.markForCheck();
      return;
    }

    // Validación para asegurar que se selecciona al menos una carrera
    if (this.newSubjectDegreeIds.length === 0) {
      this.errorMessage = 'You must select at least one major for the course.';
      this.cdr.markForCheck();
      return;
    }

    // Llamamos al servicio para crear el subject en Spring Boot
    this.subjectService
      .createSubject({
        name: this.newSubjectName,
        degreeIds: this.newSubjectDegreeIds,
        teacherId: this.newSubjectTeacherId,
      })
      .subscribe({
        next: (response) => {
          this.successMessage = response.message || 'Subject created successfully!';
          this.newSubjectName = '';
          this.newSubjectDegreeIds = [];
          this.newSubjectTeacherId = null;
          this.loadSubjects();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error creating subject. Try again.';
          console.error('Error creating subject:', err);
          this.cdr.markForCheck();
        },
      });
  }

  // Actualiza la asignatura seleccionada (solo ROLE_ADMIN)
  onUpdateSubject(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.selectedEditSubject) return;

    if (!this.editSubjectName?.trim()) {
      this.errorMessage = 'Unfilled fields: Subject Name';
      this.cdr.markForCheck();
      return;
    }

    // Validación para asegurar que se selecciona al menos una carrera al editar
    if (this.editSubjectDegreeIds.length === 0) {
      this.errorMessage = 'You must select at least one major for the course.';
      this.cdr.markForCheck();
      return;
    }

    this.subjectService
      .updateSubject(this.selectedEditSubject.id, {
        name: this.editSubjectName,
        degreeIds: this.editSubjectDegreeIds,
        teacherId: this.editSubjectTeacherId,
      })
      .subscribe({
        next: (response) => {
          this.successMessage = response.message || 'Subject updated successfully!';
          this.onCancelEditSubject();
          this.loadSubjects();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Error updating subject.';
          console.error('Error updating subject:', err);
          this.cdr.markForCheck();
        },
      });
  }

  // Permite a un profesor asignarse a sí mismo a una asignatura libre
  onAssignMyself(subjectId: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.subjectService.selfAssign(subjectId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Assigned to subject successfully!';
        this.loadSubjects();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error assigning to subject.';
        console.error('Error in self-assign:', err);
        this.cdr.markForCheck();
      },
    });
  }

  // Permite a un profesor desvincularse de una asignatura a la que está asignado
  onUnassignMyself(subjectId: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    this.subjectService.selfUnassign(subjectId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Unassigned from subject successfully!';

        // Si estábamos visualizando los alumnos de esta materia, limpiamos la lista
        this.enrolledStudents = [];
        this.selectedSubjectName = '';

        this.loadSubjects();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error unassigning from subject.';
        console.error('Error in self-unassign:', err);
        this.cdr.markForCheck();
      },
    });
  }

  // Carga y muestra los alumnos matriculados en una materia concreta
  onViewStudents(subject: Subject): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.selectedSubjectName = subject.subjectName;

    this.subjectService.getStudentsInSubject(subject.id).subscribe({
      next: (data) => {
        this.enrolledStudents = data;
        this.applyStudentsSort();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage =
          'Could not load student list. You must be the teacher or administrator.';
        console.error('Error loading students:', err);
        this.enrolledStudents = [];
        this.cdr.markForCheck();
      },
    });
  }

  // Elimina una asignatura del sistema (solo ROLE_ADMIN)
  onDeleteSubject(subjectId: number): void {
    if (
      !confirm(
        'Are you sure you want to delete this subject? This will remove all enrollments and grades associated with it.',
      )
    ) {
      return;
    }
    this.successMessage = '';
    this.errorMessage = '';
    this.subjectService.deleteSubject(subjectId).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Subject deleted successfully!';
        this.loadSubjects();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error deleting subject.';
        console.error('Error deleting subject:', err);
        this.cdr.markForCheck();
      },
    });
  }

  // Ordena la lista de alumnos matriculados por la columna seleccionada
  sortStudents(column: string): void {
    if (this.studentsSortColumn === column) {
      this.studentsSortAscending = !this.studentsSortAscending;
    } else {
      this.studentsSortColumn = column;
      this.studentsSortAscending = true;
    }
    this.applyStudentsSort();
  }

  // Aplica la ordenación a la lista de alumnos
  applyStudentsSort(): void {
    if (!this.studentsSortColumn) return;
    this.enrolledStudents.sort((a, b) => {
      let valA: any = null;
      let valB: any = null;

      switch (this.studentsSortColumn) {
        case 'id':
          valA = a.id;
          valB = b.id;
          break;
        case 'fullName':
          valA = `${a.firstName} ${a.lastName}`.toLowerCase();
          valB = `${b.firstName} ${b.lastName}`.toLowerCase();
          break;
        case 'email':
          valA = (a.academicEmail || '').toLowerCase();
          valB = (b.academicEmail || '').toLowerCase();
          break;
        default:
          return 0;
      }

      if (valA === null || valA === undefined) return 1;
      if (valB === null || valB === undefined) return -1;
      if (valA === valB) return 0;

      const comparison = valA < valB ? -1 : 1;
      return this.studentsSortAscending ? comparison : -comparison;
    });
  }
}
