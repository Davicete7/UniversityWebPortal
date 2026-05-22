import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GradeService, Grade } from '../../services/grade/grade';

@Component({
  selector: 'app-my-grades',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './myGrades.html',
  styleUrl: './myGrades.css',
})
export class MyGradesComponent implements OnInit {
  // Lista de calificaciones del alumno
  grades: Grade[] = [];

  // Filtro de búsqueda
  searchQuery: string = '';

  // Control de ordenación
  sortField: string = 'dateGiven';
  sortAscending: boolean = false; // Por defecto mostramos las más recientes primero

  // Mensaje de error
  errorMessage: string = '';

  constructor(
    private gradeService: GradeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadMyGrades();
  }

  // Carga todas las notas del alumno autenticado
  loadMyGrades(): void {
    this.errorMessage = '';
    this.gradeService.getMyGrades().subscribe({
      next: (data) => {
        this.grades = data;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load your grades. Please check back later.';
        console.error(err);
        this.cdr.markForCheck();
      }
    });
  }

  // Retorna las notas ordenadas y filtradas (Fase 5 - Búsqueda y Ordenación)
  getFilteredGrades(): Grade[] {
    let filtered = this.grades;

    if (this.searchQuery?.trim()) {
      const query = this.searchQuery.toLowerCase().trim();
      filtered = this.grades.filter(
        g => g.evaluatedSubject.subjectName.toLowerCase().includes(query) ||
             (g.gradedByTeacher && 
              (g.gradedByTeacher.firstName + ' ' + g.gradedByTeacher.lastName).toLowerCase().includes(query)) ||
             (g.gradeValue !== null && g.gradeValue !== undefined ? g.gradeValue.toString().includes(query) : ('pending'.includes(query) || 'pendiente'.includes(query)))
      );
    }

    return filtered.sort((a, b) => {
      let comparison = 0;
      if (this.sortField === 'subject') {
        comparison = a.evaluatedSubject.subjectName.localeCompare(b.evaluatedSubject.subjectName);
      } else if (this.sortField === 'value') {
        const valA = a.gradeValue !== null && a.gradeValue !== undefined ? a.gradeValue : -1;
        const valB = b.gradeValue !== null && b.gradeValue !== undefined ? b.gradeValue : -1;
        comparison = valA - valB;
      } else if (this.sortField === 'dateGiven') {
        const timeA = a.dateGiven ? new Date(a.dateGiven).getTime() : 0;
        const timeB = b.dateGiven ? new Date(b.dateGiven).getTime() : 0;
        comparison = timeA - timeB;
      } else if (this.sortField === 'teacher') {
        const nameA = a.gradedByTeacher ? (a.gradedByTeacher.firstName + ' ' + a.gradedByTeacher.lastName) : '';
        const nameB = b.gradedByTeacher ? (b.gradedByTeacher.firstName + ' ' + b.gradedByTeacher.lastName) : '';
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

  // Calcula el promedio general del estudiante (excluyendo notas pendientes)
  getGPA(): number {
    const graded = this.grades.filter(g => g.gradeValue !== null && g.gradeValue !== undefined);
    if (graded.length === 0) return 0;
    const sum = graded.reduce((acc, g) => acc + g.gradeValue, 0);
    return sum / graded.length;
  }

  // Retorna la cantidad de asignaturas calificadas (con nota no nula)
  getGradedCount(): number {
    return this.grades.filter(g => g.gradeValue !== null && g.gradeValue !== undefined).length;
  }
}
