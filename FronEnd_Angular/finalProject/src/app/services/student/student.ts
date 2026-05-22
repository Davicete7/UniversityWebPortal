import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subject } from '../subject/subject';

// Interfaz para representar la información de los compañeros de clase
export interface Classmate {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  academicEmail: string;
}

@Injectable({
  providedIn: 'root',
})
export class StudentService {
  // URL base para los endpoints del portal del estudiante en Spring Boot
  private baseUrl = 'http://localhost:8080/api/student';

  constructor(private http: HttpClient) {}

  // Permite a un estudiante matricularse en una asignatura
  enrollInSubject(subjectId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/enroll/${subjectId}`, {}, { responseType: 'text' });
  }

  // Permite a un estudiante desmatricularse de una asignatura
  unenrollFromSubject(subjectId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/unenroll/${subjectId}`, { responseType: 'text' });
  }

  // Obtiene las asignaturas en las que está matriculado el estudiante actual
  getMySubjects(): Observable<Subject[]> {
    return this.http.get<Subject[]>(`${this.baseUrl}/my-subjects`);
  }

  // Obtiene la lista de compañeros matriculados en la misma asignatura
  getClassmates(subjectId: number): Observable<Classmate[]> {
    return this.http.get<Classmate[]>(`${this.baseUrl}/subject/${subjectId}/classmates`);
  }
}
