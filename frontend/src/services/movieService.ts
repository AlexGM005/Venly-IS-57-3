import type { Movie, MoviePayload, Room } from '@/types/movie';
import { request } from './httpClient';

export const MAX_IMAGE_BYTES = 10 * 1024 * 1024;
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png'] as const;

export function getMovies(): Promise<Movie[]> {
  return request<Movie[]>('/peliculas');
}

export function getMovie(id: number): Promise<Movie> {
  return request<Movie>(`/peliculas/${id}`);
}

export function createMovie(payload: MoviePayload): Promise<Movie> {
  return request<Movie>('/peliculas', { method: 'POST', body: payload });
}

export function updateMovie(id: number, payload: MoviePayload): Promise<Movie> {
  return request<Movie>(`/peliculas/${id}`, { method: 'PUT', body: payload });
}

export function getRooms(): Promise<Room[]> {
  return request<Room[]>('/peliculas/salas');
}

/** Builds a data URL for <img> from the stored base64 image (null when the movie has no image). */
export function movieImageSrc(movie: Pick<Movie, 'imagenBase64' | 'imagenTipo'>): string | null {
  return movie.imagenBase64 && movie.imagenTipo ? `data:${movie.imagenTipo};base64,${movie.imagenBase64}` : null;
}
