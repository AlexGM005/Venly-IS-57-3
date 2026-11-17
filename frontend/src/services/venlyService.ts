/**
 * venlyService.ts
 * Punto único de conexión entre el frontend y el backend de Venly.
 * Contiene el cliente HTTP base y todos los métodos de la API REST.
 */

import { getSessionProfileId } from '@/composables/useSession';
import type { ApiErrorResponse, LoginPayload, Profile, RegisterPayload, UpdateProfilePayload } from '@/types/profile';
import type { Movie, MoviePayload, Room } from '@/types/movie';
import type { CreateReservationPayload, Reservation, SeatMap } from '@/types/reservation';

// ─────────────────────────────────────────────
// HTTP CLIENT (infraestructura base)
// ─────────────────────────────────────────────

const API_BASE_URL = '/api';
const SESSION_HEADER = 'X-Perfil-Id';

export class HttpError extends Error {
  status: number;
  payload?: ApiErrorResponse;

  constructor(status: number, message: string, payload?: ApiErrorResponse) {
    super(message);
    this.name = 'HttpError';
    this.status = status;
    this.payload = payload;
  }
}

type UnauthorizedHandler = () => void;
let unauthorizedHandler: UnauthorizedHandler | null = null;

/** Se llama cuando una petición con sesión recibe 401 (el perfil almacenado ya no existe). */
export function onSessionRejected(handler: UnauthorizedHandler) {
  unauthorizedHandler = handler;
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  body?: unknown;
  /** Envía la cabecera X-Perfil-Id de la sesión activa (por defecto true). */
  withSession?: boolean;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, withSession = true } = options;

  const destinoControlador = path.startsWith('/auth')
    ? 'AuthController.java'
    : path.startsWith('/perfiles')
      ? 'ProfileController.java'
      : path.startsWith('/funciones')
        ? 'SeatController.java'
        : path.startsWith('/reservas')
          ? 'ReservationController.java'
          : 'MovieController.java';

  console.log(`\n[VENLY TRACE - FRONTEND] 🔵 Acción: Petición ${method} ${path}\nDe: venlyService.ts -> Hacia: ${destinoControlador}`);

  const headers: Record<string, string> = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  const sessionId = withSession ? getSessionProfileId() : null;
  if (sessionId !== null) headers[SESSION_HEADER] = String(sessionId);

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    });
  } catch {
    throw new HttpError(0, 'No se pudo conectar con el servidor. Verifique que el backend esté activo.');
  }

  if (!response.ok) {
    let payload: ApiErrorResponse | undefined;
    try {
      payload = (await response.json()) as ApiErrorResponse;
    } catch {
      payload = undefined;
    }
    if (response.status === 401 && sessionId !== null) unauthorizedHandler?.();
    throw new HttpError(response.status, payload?.mensaje ?? `Error inesperado (${response.status})`, payload);
  }

  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

/** Extrae un mensaje legible y errores por campo desde cualquier error lanzado. */
export function parseApiError(error: unknown, fallback: string): { message: string; fields: Record<string, string> } {
  if (error instanceof HttpError) {
    return { message: error.payload?.mensaje ?? error.message ?? fallback, fields: error.payload?.errores ?? {} };
  }
  return { message: fallback, fields: {} };
}

// ─────────────────────────────────────────────
// AUTH
// ─────────────────────────────────────────────

export function register(payload: RegisterPayload): Promise<Profile> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Registro de perfil y asignación de contraseña\nDe: venlyService.ts -> Hacia: AuthController.java');
  return request<Profile>('/auth/register', { method: 'POST', body: payload, withSession: false });
}

export function login(payload: LoginPayload): Promise<Profile> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Solicitud de inicio de sesión\nDe: venlyService.ts -> Hacia: AuthController.java');
  return request<Profile>('/auth/login', { method: 'POST', body: payload, withSession: false });
}

// ─────────────────────────────────────────────
// PERFILES
// ─────────────────────────────────────────────

export function getMyProfile(): Promise<Profile> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de perfil propio\nDe: venlyService.ts -> Hacia: ProfileController.java');
  return request<Profile>('/perfiles/me');
}

export function updateMyProfile(payload: UpdateProfilePayload): Promise<Profile> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Actualización de perfil propio\nDe: venlyService.ts -> Hacia: ProfileController.java');
  return request<Profile>('/perfiles/me', { method: 'PUT', body: payload });
}

export function getAllProfiles(): Promise<Profile[]> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Listado de perfiles (solo Staff)\nDe: venlyService.ts -> Hacia: ProfileController.java');
  return request<Profile[]>('/perfiles');
}

export function getProfileById(id: number): Promise<Profile> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Detalle de perfil por id (Cliente vs Staff)\nDe: venlyService.ts -> Hacia: ProfileController.java');
  return request<Profile>(`/perfiles/${id}`);
}

// ─────────────────────────────────────────────
// PELÍCULAS
// ─────────────────────────────────────────────

export const MAX_IMAGE_BYTES = 10 * 1024 * 1024;
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png'] as const;

export function getMovies(): Promise<Movie[]> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de cartelera\nDe: venlyService.ts -> Hacia: MovieController.java');
  return request<Movie[]>('/peliculas');
}

export function getMovie(id: number): Promise<Movie> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de película por id\nDe: venlyService.ts -> Hacia: MovieController.java');
  return request<Movie>(`/peliculas/${id}`);
}

export function createMovie(payload: MoviePayload): Promise<Movie> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Creación de película (Gerente/Jefe)\nDe: venlyService.ts -> Hacia: MovieController.java');
  return request<Movie>('/peliculas', { method: 'POST', body: payload });
}

export function updateMovie(id: number, payload: MoviePayload): Promise<Movie> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Edición de película (Gerente/Jefe)\nDe: venlyService.ts -> Hacia: MovieController.java');
  return request<Movie>(`/peliculas/${id}`, { method: 'PUT', body: payload });
}

export function getRooms(): Promise<Room[]> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de salas disponibles\nDe: venlyService.ts -> Hacia: MovieController.java');
  return request<Room[]>('/peliculas/salas');
}

/** Construye una data URL para <img> a partir de la imagen base64 de la película (null si no tiene imagen). */
export function movieImageSrc(movie: Pick<Movie, 'imagenBase64' | 'imagenTipo'>): string | null {
  return movie.imagenBase64 && movie.imagenTipo ? `data:${movie.imagenTipo};base64,${movie.imagenBase64}` : null;
}

// ─────────────────────────────────────────────
// ASIENTOS
// ─────────────────────────────────────────────

export function getSeatMap(peliculaId: number): Promise<SeatMap> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de mapa de asientos (GET)\nDe: venlyService.ts -> Hacia: SeatController.java');
  return request<SeatMap>(`/funciones/${peliculaId}/asientos`);
}

// ─────────────────────────────────────────────
// RESERVAS
// ─────────────────────────────────────────────

export function createReservation(payload: CreateReservationPayload): Promise<Reservation> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Registro de reserva (POST)\nDe: venlyService.ts -> Hacia: ReservationController.java');
  return request<Reservation>('/reservas', { method: 'POST', body: payload });
}

export function getReservation(id: number): Promise<Reservation> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de reserva por id (GET)\nDe: venlyService.ts -> Hacia: ReservationController.java');
  return request<Reservation>(`/reservas/${id}`);
}

export function listReservations(): Promise<Reservation[]> {
  console.log('\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de reservas (GET)\nDe: venlyService.ts -> Hacia: ReservationController.java');
  return request<Reservation[]>('/reservas');
}
