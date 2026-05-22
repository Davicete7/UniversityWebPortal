import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth';
import { LoginRequest } from '../../models/authModel';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule], // Necesario para usar la directiva ngModel en formularios basados en plantillas de Angular
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  // El modelo de credenciales que se enlazará con los campos de entrada del formulario de inicio de sesión
  credentials: LoginRequest = { username: '', password: '' };

  // Variable para almacenar y mostrar mensajes de error cuando falla la autenticación
  errorMessage: string = '';

  // Inyectamos el servicio de autenticación, el enrutador para la navegación, y el detector de cambios (cdr)
  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  // Se ejecuta al pulsar el botón de envío del formulario de inicio de sesión
  onSubmit(): void {
    // Limpiamos cualquier error previo
    this.errorMessage = '';

    // Iniciamos la petición HTTP al endpoint de login
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        // Guardamos los datos de sesión y token JWT devueltos en el almacenamiento local del navegador
        localStorage.setItem('auth-token', response.token);
        localStorage.setItem('username', response.username);
        localStorage.setItem('role', response.roles[0]);
        // Guardamos el ID del usuario para poder identificarlo (por ejemplo, saber si un profesor está asignado a una materia)
        localStorage.setItem('userId', response.id.toString());

        console.log('Login successful! Welcome,', response.username);

        // Redirigimos al panel de control (Dashboard)
        this.router.navigate(['/dashboard']);
        
        // Notificamos a Angular de la actualización en caso de que requiera pintar algún estado final
        this.cdr.markForCheck();
      },
      error: (err) => {
        // En caso de credenciales incorrectas, guardamos el mensaje de error para mostrarlo en rojo en la pantalla
        this.errorMessage = 'Invalid credentials or server unavailable.';
        console.error('Login failed:', err);
        
        // Forzamos manualmente la ejecución del ciclo de detección de cambios de Angular
        // Esto garantiza que el mensaje de error aparezca al instante en el contenedor de alerta de la interfaz
        this.cdr.markForCheck();
      },
    });
  }
}
