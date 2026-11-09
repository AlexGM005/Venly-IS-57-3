export type Role = 'CLIENTE' | 'GERENTE' | 'JEFE';

export interface Profile {
  id: number;
  nombre: string;
  cedula: string;
  correo: string;
  telefono: string;
  rol: Role;
}

export interface RegisterPayload {
  nombre: string;
  cedula: string;
  correo: string;
  telefono: string;
  contrasena: string;
  rol: Role;
  claveOrganizacion?: string;
}

export interface LoginPayload {
  correo: string;
  contrasena: string;
}

export interface UpdateProfilePayload {
  nombre: string;
  correo: string;
  telefono: string;
}

export interface ApiErrorResponse {
  codigo: number;
  mensaje: string;
  errores?: Record<string, string> | null;
}
