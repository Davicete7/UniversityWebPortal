import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SignupRequest, MessageResponse } from '../../models/adminModel';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  // La URL base de tu controlador Admin en Spring Boot
  private adminUrl = 'http://localhost:8080/api/admin/';

  // Inyectamos el HttpClient
  constructor(private http: HttpClient) {}

  /**
   * Envía los datos de un nuevo usuario al servidor para registrarlo.
   * Fíjate que NO necesitamos enviar el token aquí manualmente,
   * ¡nuestro auth.interceptor.ts se encarga de pegarlo automáticamente!
   */
  createUser(userData: SignupRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.adminUrl + 'create-user', userData);
  }

  // Obtiene la lista de todos los usuarios registrados
  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(this.adminUrl + 'users');
  }

  // Modifica los datos de un usuario por su ID
  updateUser(id: number, userData: any): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${this.adminUrl}users/${id}`, userData);
  }

  // Elimina un usuario del sistema por su ID
  deleteUser(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.adminUrl}users/${id}`);
  }

  // Matricula a un estudiante en una asignatura
  enrollStudent(studentId: number, subjectId: number): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.adminUrl}users/${studentId}/enroll/${subjectId}`, {});
  }

  // Desmatricula a un estudiante de una asignatura
  unenrollStudent(studentId: number, subjectId: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.adminUrl}users/${studentId}/unenroll/${subjectId}`);
  }

  // Obtiene las calificaciones de un estudiante
  getStudentGrades(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}users/${studentId}/grades`);
  }

  // Obtiene las asignaturas matriculadas de un estudiante
  getStudentSubjects(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}users/${studentId}/subjects`);
  }

  // Añade una calificación a un alumno
  addGrade(studentId: number, gradeData: { subjectId: number; gradeValue: number }): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.adminUrl}users/${studentId}/grades`, gradeData);
  }

  // Elimina una calificación específica de un alumno
  deleteGrade(gradeId: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.adminUrl}users/grades/${gradeId}`);
  }

  // Obtiene todas las carreras registradas en el sistema
  getAllDegrees(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/degrees/all');
  }

  // Crea una carrera nueva
  createDegree(degreeData: { name: string }): Observable<MessageResponse> {
    return this.http.post<MessageResponse>('http://localhost:8080/api/degrees/create', degreeData);
  }

  // Elimina una carrera existente por su ID
  deleteDegree(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`http://localhost:8080/api/degrees/${id}`);
  }

  // Modifica los datos de una carrera existente por su ID
  updateDegree(id: number, degreeData: any): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`http://localhost:8080/api/degrees/${id}`, degreeData);
  }

  // Asigna un estudiante o profesor a una carrera específica
  assignUserToDegree(degreeId: number, userId: number): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`http://localhost:8080/api/degrees/${degreeId}/assign-user/${userId}`, {});
  }

  // Obtiene la lista de usuarios asignables (accesible para Admin y Profesores)
  getAssignableUsers(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/degrees/users');
  }
}
