import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface representing a Subject in the frontend
export interface Subject {
  id: number;
  subjectName: string;
  assignedTeacher?: {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
  } | null;
  degrees?: Array<{
    id: number;
    name: string;
    faculty: string;
  }> | null;
}

// Interface representing a student enrolled in a subject
export interface EnrolledStudent {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  academicEmail: string;
}

@Injectable({
  providedIn: 'root',
})
export class SubjectService {
  // URL base para los endpoints de asignaturas en Spring Boot
  private baseUrl = 'http://localhost:8080/api/subjects';

  constructor(private http: HttpClient) {}

  // Obtiene la lista completa de todas las asignaturas registradas
  getAllSubjects(): Observable<Subject[]> {
    return this.http.get<Subject[]>(`${this.baseUrl}/all`);
  }

  // Crea una nueva asignatura en el sistema (Permiso: ROLE_ADMIN)
  createSubject(subjectData: { name: string; degreeIds?: number[]; teacherId?: number | null }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/create`, subjectData);
  }

  // Actualiza una asignatura existente (Permiso: ROLE_ADMIN)
  updateSubject(id: number, subjectData: { name: string; degreeIds?: number[]; teacherId?: number | null }): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}`, subjectData);
  }

  // Asigna al profesor autenticado a la asignatura especificada (Permiso: ROLE_TEACHER)
  selfAssign(subjectId: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${subjectId}/self-assign`, {});
  }

  // Desasigna al profesor autenticado de la asignatura especificada (Permiso: ROLE_TEACHER)
  selfUnassign(subjectId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${subjectId}/self-unassign`);
  }

  // Obtiene la lista de estudiantes matriculados en una asignatura (Permisos: ROLE_TEACHER o ROLE_ADMIN)
  getStudentsInSubject(subjectId: number): Observable<EnrolledStudent[]> {
    return this.http.get<EnrolledStudent[]>(`${this.baseUrl}/${subjectId}/students`);
  }

  // Elimina una asignatura del sistema (Permiso: ROLE_ADMIN)
  deleteSubject(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`);
  }
}
