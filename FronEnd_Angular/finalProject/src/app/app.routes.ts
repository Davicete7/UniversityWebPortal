import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { UserManagementComponent } from './components/admin/userManagement/userManagement';
import { SubjectManagementComponent } from './components/subjectManagement/subjectManagement';
import { StudentPortalComponent } from './components/studentPortal/studentPortal';
import { TeacherGradingComponent } from './components/teacherGrading/teacherGrading';
import { MyGradesComponent } from './components/myGrades/myGrades';
import { ProfileComponent } from './components/profile/profile';
import { authGuard } from './guards/auth/authGuard';
import { DashboardHomeComponent } from './components/dashboardHome/dashboardHome';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard],
    // Definimos las rutas secundarias que se renderizan dinámicamente dentro de la sección de contenido del Dashboard
    children: [
      { path: 'manageUsers', component: UserManagementComponent, data: { tab: 'directory' } },
      { path: 'createUser', component: UserManagementComponent, data: { tab: 'create' } },
      { path: 'degreeManagement', component: UserManagementComponent, data: { tab: 'degrees' } },
      { path: 'manageSubjects', component: SubjectManagementComponent }, // Ruta para la gestión de asignaturas
      { path: 'studentPortal', component: StudentPortalComponent }, // Ruta para portal del estudiante
      { path: 'grading', component: TeacherGradingComponent }, // Ruta para panel del profesor para calificar
      { path: 'myGrades', component: MyGradesComponent }, // Ruta para consultar calificaciones
      { path: 'profile', component: ProfileComponent }, // Ruta para la edición de perfil (Fase 5)
      { path: '', component: DashboardHomeComponent }
    ],
  },


  { path: '', redirectTo: 'login', pathMatch: 'full' },
];
