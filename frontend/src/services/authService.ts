import type { LoginPayload, Profile, RegisterPayload } from '@/types/profile';
import { request } from './httpClient';

export function register(payload: RegisterPayload): Promise<Profile> {
  console.log("[VENLY TRACE - FRONTEND] Acción: Procesando registro de perfil y asignación de contraseña en frontend/src/services/authService.ts -> dirigiendo a frontend/src/services/httpClient.ts");
  return request<Profile>('/auth/register', { method: 'POST', body: payload, withSession: false });
}

export function login(payload: LoginPayload): Promise<Profile> {
  console.log("[VENLY TRACE - FRONTEND] Acción: Procesando solicitud de inicio de sesión en frontend/src/services/authService.ts -> dirigiendo a frontend/src/services/httpClient.ts");
  return request<Profile>('/auth/login', { method: 'POST', body: payload, withSession: false });
}
