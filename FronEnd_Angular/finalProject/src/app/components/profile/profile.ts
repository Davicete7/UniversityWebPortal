import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../../services/profile/profile';

/**
 * Componente standalone para gestionar el perfil del usuario.
 * Permite editar el nombre, apellido, número de teléfono, biografía,
 * cambiar la contraseña y subir/eliminar una foto de perfil en Base64.
 * Sigue la regla de no deshabilitar botones de envío y mostrar alertas en su lugar.
 */
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfileComponent implements OnInit {
  // Datos del perfil recuperados del backend
  profileData: any = {
    username: '',
    firstName: '',
    lastName: '',
    academicEmail: '',
    phoneNumber: '',
    bio: '',
    avatar: null,
    role: '',
    degreeName: ''
  };

  // Campos locales para la edición
  firstName: string = '';
  lastName: string = '';
  phoneNumber: string = '';
  bio: string = '';
  avatar: string | null = null;

  // Gestión de cambio de contraseña
  newPassword: string = '';
  confirmPassword: string = '';

  // Mensajes de estado
  successMessage: string = '';
  errorMessages: string[] = [];

  constructor(
    private profileService: ProfileService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  /**
   * Carga los detalles del perfil desde el backend e inicializa las variables editables.
   */
  loadProfile(): void {
    this.errorMessages = [];
    this.profileService.getProfile().subscribe({
      next: (data) => {
        this.profileData = data;
        this.firstName = data.firstName || '';
        this.lastName = data.lastName || '';
        this.phoneNumber = data.phoneNumber || '';
        this.bio = data.bio || '';
        this.avatar = data.avatar || null;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessages = ['Failed to load profile details. Please try again.'];
        console.error(err);
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Maneja la selección de imagen y la convierte a una cadena Base64
   * para poder ser almacenada en la base de datos (columna de tipo TEXT).
   */
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) { // Límite de 2MB para evitar sobrecarga
        this.errorMessages = ['The selected image is too large. Max size allowed is 2MB.'];
        this.cdr.markForCheck();
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        this.avatar = reader.result as string;
        this.cdr.markForCheck();
      };
      reader.readAsDataURL(file);
    }
  }

  /**
   * Elimina el avatar actual del usuario.
   */
  removeAvatar(): void {
    this.avatar = null;
    this.cdr.markForCheck();
  }

  /**
   * Valida la solicitud de actualización y la envía al backend.
   * Si hay campos inválidos, se muestran en una lista de errores y no se envía la petición.
   */
  onSubmit(): void {
    this.errorMessages = [];
    this.successMessage = '';

    const missingFields: string[] = [];

    // Validación de campos obligatorios
    if (!this.firstName.trim()) {
      missingFields.push('First Name');
    }
    if (!this.lastName.trim()) {
      missingFields.push('Last Name');
    }

    if (missingFields.length > 0) {
      this.errorMessages.push(`Missing fields: ${missingFields.join(', ')}`);
    }

    // Validación de cambio de contraseña
    if (this.newPassword.trim() || this.confirmPassword.trim()) {
      if (this.newPassword.length < 6) {
        this.errorMessages.push('Password must be at least 6 characters.');
      }
      if (this.newPassword !== this.confirmPassword) {
        this.errorMessages.push('Passwords do not match.');
      }
    }

    // Si existen errores, abortamos la petición para mostrar la alerta
    if (this.errorMessages.length > 0) {
      this.cdr.markForCheck();
      return;
    }

    // Preparar el cuerpo de la petición
    const updateBody: any = {
      firstName: this.firstName,
      lastName: this.lastName,
      phoneNumber: this.phoneNumber,
      bio: this.bio,
      avatar: this.avatar
    };

    // Si se especificó una nueva contraseña válida, se añade a la solicitud
    if (this.newPassword.trim()) {
      updateBody.password = this.newPassword;
    }

    this.profileService.updateProfile(updateBody).subscribe({
      next: (res) => {
        this.successMessage = res.message || 'Profile updated successfully!';
        this.newPassword = '';
        this.confirmPassword = '';
        this.loadProfile(); // Recargamos para refrescar la información
      },
      error: (err) => {
        const backendError = err.error?.message || 'Error updating profile. Please try again.';
        this.errorMessages.push(backendError);
        this.cdr.markForCheck();
      }
    });
  }
}
