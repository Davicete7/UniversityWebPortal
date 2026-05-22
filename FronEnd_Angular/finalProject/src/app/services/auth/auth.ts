import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// Aquí está tu nombre personalizado
import { LoginRequest, JwtResponse } from '../../models/authModel';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // La URL base de tu servidor Spring Boot
  private authUrl = 'http://localhost:8080/api/auth/';

  // Inyectamos el HttpClient en el constructor
  constructor(private http: HttpClient) {}

  /**
   * Envía las credenciales al backend para iniciar sesión.
   * Devuelve un Observable que contendrá el JwtResponse si todo va bien.
   */
  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(this.authUrl + 'signin', credentials);
  }
}
