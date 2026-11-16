import type { CreateReservationPayload, Reservation } from '@/types/reservation';
import { request } from './httpClient';

export function createReservation(payload: CreateReservationPayload): Promise<Reservation> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Registro de reserva (POST)\nDe: reservationService.ts -> Hacia: httpClient.ts");
  return request<Reservation>('/reservas', { method: 'POST', body: payload });
}

export function getReservation(id: number): Promise<Reservation> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de reserva por id (GET)\nDe: reservationService.ts -> Hacia: httpClient.ts");
  return request<Reservation>(`/reservas/${id}`);
}

export function listReservations(): Promise<Reservation[]> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de reservas (GET)\nDe: reservationService.ts -> Hacia: httpClient.ts");
  return request<Reservation[]>('/reservas');
}
