import type { Profile, UpdateProfilePayload } from '@/types/profile';
import { request } from './httpClient';

export function getMyProfile(): Promise<Profile> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de perfil propio\nDe: profileService.ts -> Hacia: httpClient.ts");
  return request<Profile>('/perfiles/me');
}

export function updateMyProfile(payload: UpdateProfilePayload): Promise<Profile> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Actualización de perfil propio\nDe: profileService.ts -> Hacia: httpClient.ts");
  return request<Profile>('/perfiles/me', { method: 'PUT', body: payload });
}

export function getAllProfiles(): Promise<Profile[]> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Listado de perfiles (solo Staff)\nDe: profileService.ts -> Hacia: httpClient.ts");
  return request<Profile[]>('/perfiles');
}

export function getProfileById(id: number): Promise<Profile> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Detalle de perfil por id (Cliente vs Staff)\nDe: profileService.ts -> Hacia: httpClient.ts");
  return request<Profile>(`/perfiles/${id}`);
}
