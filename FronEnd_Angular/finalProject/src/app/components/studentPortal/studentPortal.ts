import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StudentService, Classmate } from '../../services/student/student';
import { SubjectService, Subject } from '../../services/subject/subject';
import { ProfileService } from '../../services/profile/profile';

@Component({
  selector: 'app-student-portal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './studentPortal.html',
  styleUrl: './studentPortal.css',
})
export class StudentPortalComponent implements OnInit {
  // Catálogo completo de asignaturas disponibles en la universidad
  allSubjects: Subject[] = [];

  // Lista de asignaturas en las que está matriculado el estudiante actual
  mySubjects: Subject[] = [];

  // Filtro de texto para buscar asignaturas en tiempo real
  searchQuery: string = '';

  // Control del orden actual de la tabla (nombre o ID)
  sortField: string = 'subjectName';
  sortAscending: boolean = true;

  // Lista de compañeros de clase de la asignatura seleccionada
  classmates: Classmate[] = [];
  selectedSubjectName: string = '';

  // Mensajes de feedback visual
  successMessage: string = '';
  errorMessage: string = '';

  // ID de la carrera del estudiante para filtrar asignaturas
  studentDegreeId: number | null = null;

  // ID de la carrera del estudiante para filtrar asignaturas
  studentDegree: string = '';

  constructor(
    private studentService: StudentService,
    private subjectService: SubjectService,
    private profileService: ProfileService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadAllData();
  }

  // Carga todas las asignaturas de la universidad y las asignaturas matriculadas del estudiante
  loadAllData(): void {
    this.successMessage = '';
    this.errorMessage = '';

    // Cargar el perfil del estudiante para obtener su carrera (degree)
    this.profileService.getProfile().subscribe({
      next: (profile: any) => {
        this.studentDegree = profile?.degreeName || null;
        this.cdr.markForCheck();
      },
    });

    // Cargamos primero mis asignaturas y luego todas las asignaturas
    this.studentService.getMySubjects().subscribe({
      next: (mySubs) => {
        this.mySubjects = mySubs;
        this.loadCatalog();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load your enrolled subjects.';
        console.error(err);
        this.cdr.markForCheck();
      },
    });

    // Cargar el perfil del estudiante para obtener su carrera (degree)
    this.profileService.getProfile().subscribe({
      next: (profile: any) => {
        this.studentDegreeId = profile?.degree?.id || null;
        this.cdr.markForCheck();
      },
    });


  }

  // Carga el catálogo general de asignaturas
  // Load the general subject catalog and filter it by the current degree
  // Load the general subject catalog
  loadCatalog(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (allSubs) => {

        this.allSubjects = allSubs.filter(subject => {
          // SOLUCIÓN: Añadimos '?' justo después de 'degrees'
          return subject.degrees?.some(degree => degree.name === this.studentDegree);
        });

        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error loading the catalog:', err);
      }
    });
  }

  // Comprueba si el estudiante ya está matriculado en una asignatura específica
  isEnrolled(subjectId: number): boolean {
    return this.mySubjects.some((sub) => sub.id === subjectId);
  }

  // Ejecuta el proceso de matrícula de una asignatura tras confirmar que es irrevocable
  onEnroll(subjectId: number): void {
    const confirmacion = confirm(
      'Are you sure you want to enroll in this course? This action is irreversible and will require administrator intervention to undo.',
    );
    if (!confirmacion) {
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';

    this.studentService.enrollInSubject(subjectId).subscribe({
      next: (response) => {
        this.successMessage = response || 'Enrolled successfully!';
        this.loadAllData(); // Recargamos para actualizar las tablas
      },
      error: (err) => {
        this.errorMessage = err.error || 'Failed to enroll in the subject. Please try again.';
        console.error('Error during enrollment:', err);
        this.cdr.markForCheck();
      },
    });
  }

  // Carga la lista de compañeros de clase matriculados en una asignatura
  onViewClassmates(subject: Subject): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.selectedSubjectName = subject.subjectName;

    this.studentService.getClassmates(subject.id).subscribe({
      next: (data) => {
        this.classmates = data;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Could not retrieve classmate list.';
        console.error(err);
        this.classmates = [];
        this.cdr.markForCheck();
      },
    });
  }

  // Retorna el catálogo filtrado por el campo de búsqueda (Fase 5 - Búsqueda)
  getFilteredCatalog(): Subject[] {
    let filtered = this.allSubjects;

    // Filtrar asignaturas que pertenezcan a la carrera del estudiante
    if (this.studentDegreeId) {
      filtered = filtered.filter(
        (sub) => sub.degrees && sub.degrees.some((d) => d.id === this.studentDegreeId),
      );
    }

    if (this.searchQuery?.trim()) {
      const query = this.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(
        (sub) =>
          sub.subjectName.toLowerCase().includes(query) ||
          sub.id.toString().includes(query) ||
          (sub.assignedTeacher &&
            (sub.assignedTeacher.firstName + ' ' + sub.assignedTeacher.lastName)
              .toLowerCase()
              .includes(query)),
      );
    }

    // Aplicar ordenación (Fase 5 - Ordenación)
    return filtered.sort((a, b) => {
      let comparison = 0;
      if (this.sortField === 'id') {
        comparison = a.id - b.id;
      } else if (this.sortField === 'subjectName') {
        comparison = a.subjectName.localeCompare(b.subjectName);
      } else if (this.sortField === 'teacher') {
        const nameA = a.assignedTeacher
          ? a.assignedTeacher.firstName + ' ' + a.assignedTeacher.lastName
          : '';
        const nameB = b.assignedTeacher
          ? b.assignedTeacher.firstName + ' ' + b.assignedTeacher.lastName
          : '';
        comparison = nameA.localeCompare(nameB);
      }

      return this.sortAscending ? comparison : -comparison;
    });
  }

  // Cambia el criterio de ordenación de la tabla
  toggleSort(field: string): void {
    if (this.sortField === field) {
      this.sortAscending = !this.sortAscending;
    } else {
      this.sortField = field;
      this.sortAscending = true;
    }
    this.cdr.markForCheck();
  }
}
