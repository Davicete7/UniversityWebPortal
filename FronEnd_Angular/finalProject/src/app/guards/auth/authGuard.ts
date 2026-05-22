import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  // Inject the Router to enable navigation if the user is not authenticated
  const router = inject(Router);

  // Retrieve the token from local storage
  const token = localStorage.getItem('auth-token');

  if (token) {
    // Token exists: Access granted
    return true;
  }

  // No token found: Access denied, redirect to the login page
  router.navigate(['/login']);
  return false;
};
