// Ruta relativa a proposito: el frontend y la API se publican bajo el mismo
// origen, porque nginx sirve esta aplicacion y reenvia /api al backend (ver
// erp-frontend/nginx.conf). Al compartir origen, el navegador no hace
// peticiones cross-origin y CORS no interviene.
//
// En desarrollo con "ng serve" el proxy de proxy.conf.json cumple el mismo
// papel: manda /api al backend que corre en el puerto 8080.
export const API_URL = '/api';
