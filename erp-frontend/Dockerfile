# ===========================================================================
# Imagen del frontend: compila el Angular y lo sirve con nginx, que ademas
# hace de proxy hacia el backend (ver nginx.conf).
# ===========================================================================

# --- Etapa 1: compilar ---------------------------------------------------
FROM node:24-alpine AS build
WORKDIR /build

# npm ci en lugar de npm install: instala exactamente lo del package-lock.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Etapa 2: servir -----------------------------------------------------
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular 20+ deja los archivos del navegador en dist/<proyecto>/browser.
COPY --from=build /build/dist/erp-frontend/browser /usr/share/nginx/html

EXPOSE 80
