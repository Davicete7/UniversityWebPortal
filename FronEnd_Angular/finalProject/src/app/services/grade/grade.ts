import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subject } from '../subject/subject';

// Interfaz que representa una Calificación
export interface Grade {
  id: number;
  gradeValue: number;
  dateGiven: string;
  examType?: string;
  gradedStudent: {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    academicEmail: string;
  };
  evaluatedSubject: Subject;
  gradedByTeacher?: {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
  };
}

@Injectable({
  providedIn: 'root',
})
export class GradeService {
  // URL base para los endpoints de calificaciones en Spring Boot
  private baseUrl = 'http://localhost:8080/api/grades';

  constructor(private http: HttpClient) {}

  // Permite a un profesor calificar a un estudiante (Permiso: ROLE_TEACHER)
  assignGrade(gradeData: { studentId: number; subjectId: number; gradeValue: number }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/assign`, gradeData);
  }

  // Obtiene todas las notas asignadas por el profesor a una asignatura (Permiso: ROLE_TEACHER)
  getGradesBySubject(subjectId: number): Observable<Grade[]> {
    return this.http.get<Grade[]>(`${this.baseUrl}/teacher/subject/${subjectId}`);
  }

  // Obtiene el historial completo de calificaciones del estudiante autenticado (Permiso: ROLE_STUDENT)
  getMyGrades(): Observable<Grade[]> {
    return this.http.get<Grade[]>(`${this.baseUrl}/my-grades`);
  }

  // Permite a un profesor actualizar la calificación de un estudiante (Permiso: ROLE_TEACHER)
  updateGrade(gradeId: number, gradeData: { gradeValue: number }): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${gradeId}`, gradeData);
  }
}
