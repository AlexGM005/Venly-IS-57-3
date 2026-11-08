import type { ApiErrorResponse } from '@/types/profile';

const API_BASE_URL = 'http://localhost:8080/api';

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

interface RequestOptions extends RequestInit {
  headers?: Record<string, string>;
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    }
  });

  if (!response.ok) {
    let payload: ApiErrorResponse | undefined;
    try {
      payload = (await response.json()) as ApiErrorResponse;
    } catch {
      payload = undefined;
    }
    throw new HttpError(response.status, payload?.mensaje ?? 'Error inesperado', payload);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
