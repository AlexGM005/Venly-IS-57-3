import type { LoginPayload, Profile, RegisterPayload } from '@/types/profile';
import { request } from './httpClient';

export function register(payload: RegisterPayload): Promise<Profile> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Registro de perfil y asignación de contraseña\nDe: authService.ts -> Hacia: httpClient.ts");
  return request<Profile>('/auth/register', { method: 'POST', body: payload, withSession: false });
}

export function login(payload: LoginPayload): Promise<Profile> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Solicitud de inicio de sesión\nDe: authService.ts -> Hacia: httpClient.ts");
  return request<Profile>('/auth/login', { method: 'POST', body: payload, withSession: false });
}
