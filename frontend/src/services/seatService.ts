import type { SeatMap } from '@/types/reservation';
import { request } from './httpClient';

export function getSeatMap(peliculaId: number): Promise<SeatMap> {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de mapa de asientos (GET)\nDe: seatService.ts -> Hacia: httpClient.ts");
  return request<SeatMap>(`/funciones/${peliculaId}/asientos`);
}
