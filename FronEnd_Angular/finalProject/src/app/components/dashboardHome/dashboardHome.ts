import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin/admin';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './dashboardHome.html',
  styleUrl: './dashboardHome.css',
})
export class DashboardHomeComponent implements OnInit {
  username: string | null = '';
  userRole: string | null = '';
  adminsList: any[] = [];

  // Variables para la ordenacion de la tabla de administradores
  sortColumn: string = 'name';
  sortAscending: boolean = true;

  constructor(
    private router: Router,
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Recuperamos los datos para saber a quien estamos dando la bienvenida
    this.username = localStorage.getItem('username');
    this.userRole = localStorage.getItem('role');
    if (this.userRole === 'ROLE_ADMIN') {
      this.loadAdmins();
    }
  }

  // Carga los usuarios y filtra aquellos que tienen rol de administrador
  loadAdmins(): void {
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.adminsList = users.filter(u => u.userRole?.roleName === 'ROLE_ADMIN');
        this.sortAdmins(this.sortColumn);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error al cargar los administradores:', err);
      }
    });
  }

  // Alterna el orden de la columna seleccionada
  toggleSort(column: string): void {
    if (this.sortColumn === column) {
      this.sortAscending = !this.sortAscending;
    } else {
      this.sortColumn = column;
      this.sortAscending = true;
    }
    this.sortAdmins(column);
  }

  // Ordena la lista de administradores en memoria
  sortAdmins(column: string): void {
    this.adminsList.sort((a, b) => {
      let valA = '';
      let valB = '';

      if (column === 'name') {
        valA = `${a.firstName || ''} ${a.lastName || ''}`.trim().toLowerCase();
        valB = `${b.firstName || ''} ${b.lastName || ''}`.trim().toLowerCase();
      } else if (column === 'email') {
        valA = (a.academicEmail || '').toLowerCase();
        valB = (b.academicEmail || '').toLowerCase();
      } else if (column === 'phone') {
        valA = (a.phoneNumber || '').toLowerCase();
        valB = (b.phoneNumber || '').toLowerCase();
      }

      if (valA < valB) return this.sortAscending ? -1 : 1;
      if (valA > valB) return this.sortAscending ? 1 : -1;
      return 0;
    });
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}
