import type { Profile, UpdateProfilePayload } from '@/types/profile';
import { request } from './httpClient';

export function getMyProfile(): Promise<Profile> {
  return request<Profile>('/perfiles/me');
}

export function updateMyProfile(payload: UpdateProfilePayload): Promise<Profile> {
  return request<Profile>('/perfiles/me', { method: 'PUT', body: payload });
}

export function getAllProfiles(): Promise<Profile[]> {
  return request<Profile[]>('/perfiles');
}

export function getProfileById(id: number): Promise<Profile> {
  return request<Profile>(`/perfiles/${id}`);
}
