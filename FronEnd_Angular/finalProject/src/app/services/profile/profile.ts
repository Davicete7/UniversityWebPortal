import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

/**
 * Servicio encargado de gestionar las llamadas a la API del perfil de usuario
 * y de mantener un BehaviorSubject reactivo para transmitir las actualizaciones de avatar.
 */
@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private apiUrl = 'http://localhost:8080/api/profile';

  // BehaviorSubject reactivo para notificar cambios en la foto de perfil en tiempo real
  private avatarSubject = new BehaviorSubject<string | null>(null);
  avatar$ = this.avatarSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Recupera la información del perfil del usuario logueado.
   * Además, actualiza el flujo reactivo del avatar si el usuario tiene uno definido.
   */
  getProfile(): Observable<any> {
    return this.http.get<any>(this.apiUrl).pipe(
      tap(profile => {
        this.avatarSubject.next(profile ? profile.avatar : null);
      })
    );
  }

  /**
   * Envía los datos actualizados del perfil al backend.
   * Si se incluye una nueva foto de perfil, se notifica de forma reactiva a todos los suscriptores.
   */
  updateProfile(profileData: any): Observable<any> {
    return this.http.put<any>(this.apiUrl, profileData).pipe(
      tap(() => {
        if (profileData.avatar !== undefined) {
          this.avatarSubject.next(profileData.avatar);
        }
      })
    );
  }

  /**
   * Actualiza directamente el estado del avatar localmente en la aplicación.
   */
  updateAvatarInSubject(avatar: string | null): void {
    this.avatarSubject.next(avatar);
  }
}
