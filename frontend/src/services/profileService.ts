import type { CreateProfilePayload, Profile } from '@/types/profile';
import { request } from './httpClient';

export function createProfile(payload: CreateProfilePayload): Promise<Profile> {
  return request<Profile>('/perfiles', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function getProfileById(profileId: number, requesterId: number): Promise<Profile> {
  return request<Profile>(`/perfiles/${profileId}`, {
    method: 'GET',
    headers: {
      'X-Perfil-Id': String(requesterId)
    }
  });
}
