import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. Obtenemos el token guardado en la memoria del navegador
  const token = localStorage.getItem('auth-token');

  // 2. Si existe un token, clonamos la petición original y le añadimos la cabecera
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    // Enviamos la petición modificada hacia el servidor (Spring Boot)
    return next(clonedRequest);
  }

  // 3. Si no hay token (por ejemplo, cuando estamos haciendo login por primera vez),
  // enviamos la petición tal y como estaba.
  return next(req);
};
