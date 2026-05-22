import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink, RouterOutlet, RouterLinkActive } from '@angular/router';
import { ProfileService } from '../../services/profile/profile';
import { Subscription } from 'rxjs';

/**
 * Componente principal del panel de control (Dashboard).
 * Proporciona la estructura del menú lateral (sidebar) y la barra superior.
 * Se suscribe de manera reactiva a los cambios de foto de perfil (avatar)
 * utilizando el ProfileService para reflejar cualquier cambio de inmediato.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, RouterOutlet, RouterLinkActive],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
  })
export class DashboardComponent implements OnInit, OnDestroy {
  username: string | null = '';
  userRole: string | null = '';
  avatarUrl: string | null = null;
  private avatarSubscription!: Subscription;

  constructor(
    private router: Router,
    private profileService: ProfileService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.username = localStorage.getItem('username');
    this.userRole = localStorage.getItem('role');

    // Suscribirse a los cambios del avatar en tiempo real a través del BehaviorSubject
    // Se utiliza ChangeDetectorRef para forzar la actualización inmediata de la interfaz
    this.avatarSubscription = this.profileService.avatar$.subscribe({
      next: (url) => {
        this.avatarUrl = url;
        this.cdr.markForCheck();
      }
    });

    // Cargar los datos del perfil para activar el BehaviorSubject del avatar
    this.profileService.getProfile().subscribe({
      error: (err) => {
        console.error('Error loading profile for avatar mapping', err);
      }
    });
  }

  ngOnDestroy(): void {
    // Evitar fugas de memoria cancelando la suscripción al destruir el componente
    if (this.avatarSubscription) {
      this.avatarSubscription.unsubscribe();
    }
  }

  logout(): void {
    localStorage.clear();
    // Limpiamos el avatar en el servicio al cerrar sesión
    this.profileService.updateAvatarInSubject(null);
    this.router.navigate(['/login']);
  }
}

