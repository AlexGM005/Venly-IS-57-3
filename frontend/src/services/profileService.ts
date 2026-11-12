import type { Profile, UpdateProfilePayload } from '@/types/profile';
import { request } from './httpClient';

export function getMyProfile(): Promise<Profile> {
  console.log("[VENLY TRACE - FRONTEND] Acción: Solicitando consulta de perfil propio en frontend/src/services/profileService.ts -> dirigiendo a frontend/src/services/httpClient.ts");
  return request<Profile>('/perfiles/me');
}

export function updateMyProfile(payload: UpdateProfilePayload): Promise<Profile> {
  console.log("[VENLY TRACE - FRONTEND] Acción: Enviando actualización de perfil propio en frontend/src/services/profileService.ts -> dirigiendo a frontend/src/services/httpClient.ts");
  return request<Profile>('/perfiles/me', { method: 'PUT', body: payload });
}

export function getAllProfiles(): Promise<Profile[]> {
  console.log("[VENLY TRACE - FRONTEND] Acción: Solicitando listado completo de perfiles (restricción exclusiva de Staff) en frontend/src/services/profileService.ts -> dirigiendo a frontend/src/services/httpClient.ts");
  return request<Profile[]>('/perfiles');
}

export function getProfileById(id: number): Promise<Profile> {
  console.log("[VENLY TRACE - FRONTEND] Acción: Solicitando detalle de perfil por id (validación Cliente vs Staff) en frontend/src/services/profileService.ts -> dirigiendo a frontend/src/services/httpClient.ts");
  return request<Profile>(`/perfiles/${id}`);
}
