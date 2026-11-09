import type { LoginPayload, Profile, RegisterPayload } from '@/types/profile';
import { request } from './httpClient';

export function register(payload: RegisterPayload): Promise<Profile> {
  return request<Profile>('/auth/register', { method: 'POST', body: payload, withSession: false });
}

export function login(payload: LoginPayload): Promise<Profile> {
  return request<Profile>('/auth/login', { method: 'POST', body: payload, withSession: false });
}
