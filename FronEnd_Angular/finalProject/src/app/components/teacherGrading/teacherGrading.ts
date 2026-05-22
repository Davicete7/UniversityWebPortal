import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SubjectService, Subject, EnrolledStudent } from '../../services/subject/subject';
import { GradeService, Grade } from '../../services/grade/grade';

@Component({
  selector: 'app-teacher-grading',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teacherGrading.html',
  styleUrl: './teacherGrading.css',
})
export class TeacherGradingComponent implements OnInit {
  // Lista de todas las asignaturas asignadas a este profesor
  mySubjects: Subject[] = [];

  // Asignatura seleccionada actualmente para calificar
  selectedSubject: Subject | null = null;

  // Lista de estudiantes matriculados en la asignatura seleccionada
  enrolledStudents: EnrolledStudent[] = [];

  // Mapa para indexar las notas existentes de cada estudiante (studentId -> Grade[])
  gradesMap: { [key: number]: Grade[] } = {};

  // Modelo temporal para el formulario de asignación de notas (studentId -> nota seleccionada)
  selectedGrades: { [key: number]: number } = {};

  // ID del profesor autenticado obtenido de sesión
  userId: number | null = null;

  // Filtro de búsqueda de alumnos
  studentSearchQuery: string = '';

  // Variables para la ordenación
  studentsSortColumn: string = '';
  studentsSortAscending: boolean = true;

  // Mensajes de éxito y error
  successMessage: string = '';
  errorMessage: string = '';

  // Propiedades para la edición inline de calificaciones
  editingGradeId: number | null = null;
  editingGradeValue: number = 5.0;

  constructor(
    private subjectService: SubjectService,
    private gradeService: GradeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const storedId = localStorage.getItem('userId');
    if (storedId) {
      this.userId = parseInt(storedId, 10);
    }
    this.loadTeacherSubjects();
  }

  // Carga todas las asignaturas del sistema y filtra las asignadas a este profesor
  loadTeacherSubjects(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        if (this.userId) {
          // Filtramos las materias donde el id del profesor coincida con el del usuario actual
          this.mySubjects = data.filter(sub => sub.assignedTeacher?.id === this.userId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load your assigned subjects.';
        console.error(err);
        this.cdr.markForCheck();
      }
    });
  }

  // Se ejecuta al seleccionar una asignatura del menú o panel
  onSelectSubject(subject: Subject): void {
    this.selectedSubject = subject;
    this.successMessage = '';
    this.errorMessage = '';
    this.enrolledStudents = [];
    this.gradesMap = {};
    this.selectedGrades = {};

    this.loadStudentsAndGrades(subject.id);
  }

  // Carga concurrentemente estudiantes y notas asociadas a la materia
  loadStudentsAndGrades(subjectId: number): void {
    // 1. Obtener alumnos matriculados
    this.subjectService.getStudentsInSubject(subjectId).subscribe({
      next: (students) => {
        this.enrolledStudents = students;
        
        // Inicializamos los valores por defecto en el desplegable de notas (por ejemplo, 5.0)
        students.forEach(s => {
          this.selectedGrades[s.id] = 5.0;
        });

        // 2. Obtener calificaciones registradas en esta materia por este docente
        this.gradeService.getGradesBySubject(subjectId).subscribe({
          next: (grades) => {
            // Agrupamos las notas por ID de estudiante
            const tempMap: { [key: number]: Grade[] } = {};
            grades.forEach(g => {
              const sId = g.gradedStudent.id;
              if (!tempMap[sId]) {
                tempMap[sId] = [];
              }
              tempMap[sId].push(g);
            });
            this.gradesMap = tempMap;
            this.cdr.markForCheck();
          },
          error: (err) => {
            console.error('Failed to load grades for subject:', err);
            this.cdr.markForCheck();
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Failed to load students enrolled in this subject.';
        console.error(err);
        this.cdr.markForCheck();
      }
    });
  }

  // Proceso de asignación de una calificación a un estudiante
  onAssignGrade(studentId: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.selectedSubject) {
      this.errorMessage = 'No subject selected.';
      return;
    }

    // Regla de negocio: Comprobar si el alumno ya posee una calificación para esa asignatura
    const studentGrades = this.gradesMap[studentId] || [];
    if (studentGrades.length > 0) {
      this.errorMessage = 'Student already has a final grade for this subject. Click the grade badge to edit it.';
      this.cdr.markForCheck();
      return;
    }

    const gradeValue = this.selectedGrades[studentId];

    // Validación manual de campos obligatorios
    if (gradeValue === undefined || gradeValue === null) {
      this.errorMessage = 'Unfilled fields: Grade Value';
      this.cdr.markForCheck();
      return;
    }

    const payload = {
      studentId: studentId,
      subjectId: this.selectedSubject.id,
      gradeValue: gradeValue
    };

    this.gradeService.assignGrade(payload).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Grade assigned successfully!';
        // Recargamos los datos para refrescar la lista de calificaciones en pantalla
        if (this.selectedSubject) {
          this.loadStudentsAndGrades(this.selectedSubject.id);
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error assigning grade. Please try again.';
        console.error('Error assigning grade:', err);
        this.cdr.markForCheck();
      }
    });
  }

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

  // Ordena la lista de alumnos según la columna seleccionada
  sortStudents(column: string): void {
    if (this.studentsSortColumn === column) {
      this.studentsSortAscending = !this.studentsSortAscending;
    } else {
      this.studentsSortColumn = column;
      this.studentsSortAscending = true;
    }
    this.cdr.markForCheck();
  }

  // Filtrado y ordenación reactiva de estudiantes en la asignatura (Fase 5 - Búsqueda)
  getFilteredStudents(): EnrolledStudent[] {
    let result = [...this.enrolledStudents];
    
    // 1. Filtrado por búsqueda
    if (this.studentSearchQuery?.trim()) {
      const query = this.studentSearchQuery.toLowerCase().trim();
      result = result.filter(
        s => s.firstName.toLowerCase().includes(query) ||
             s.lastName.toLowerCase().includes(query) ||
             s.academicEmail.toLowerCase().includes(query) ||
             s.id.toString().includes(query)
      );
    }

    // 2. Ordenación por columna
    if (this.studentsSortColumn) {
      result.sort((a, b) => {
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
          case 'grade':
            const gradesA = this.gradesMap[a.id] || [];
            const gradesB = this.gradesMap[b.id] || [];
            valA = gradesA.length > 0 ? gradesA[0].gradeValue : null;
            valB = gradesB.length > 0 ? gradesB[0].gradeValue : null;
            break;
          default:
            return 0;
        }

        return this.compareValues(valA, valB, this.studentsSortAscending);
      });
    }

    return result;
  }

  // Activa la edición de una calificación existente
  onStartEditGrade(grade: any): void {
    this.editingGradeId = grade.id;
    this.editingGradeValue = grade.gradeValue;
    this.cdr.markForCheck();
  }

  // Cancela la edición de una calificación
  onCancelEditGrade(): void {
    this.editingGradeId = null;
    this.cdr.markForCheck();
  }

  // Guarda la calificación editada
  onSaveEditGrade(gradeId: number): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.gradeService.updateGrade(gradeId, { gradeValue: this.editingGradeValue }).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Grade updated successfully!';
        this.editingGradeId = null;
        if (this.selectedSubject) {
          this.loadStudentsAndGrades(this.selectedSubject.id);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error updating grade.';
        this.cdr.markForCheck();
      }
    });
  }
}
