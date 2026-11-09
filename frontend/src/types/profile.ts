export type Role = 'CLIENTE' | 'GERENTE' | 'JEFE';

export interface CreateProfilePayload {
  nombre: string;
  cedula: string;
  correo: string;
  telefono: string;
  contrasena: string;
  rol: Role;
  claveOrganizacion?: string;
}

export interface Profile {
  id: number;
  nombre: string;
  cedula: string;
  correo: string;
  telefono: string;
  rol: Role;
}

export interface ApiErrorResponse {
  codigo: number;
  mensaje: string;
  errores?: Record<string, string>;
}
