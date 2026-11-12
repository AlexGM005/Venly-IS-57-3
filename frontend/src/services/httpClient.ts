import { getSessionProfileId } from '@/composables/useSession';
import type { ApiErrorResponse } from '@/types/profile';

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

/** Called when a request sent with a session gets 401 (e.g. the stored profile no longer exists). */
export function onSessionRejected(handler: UnauthorizedHandler) {
  unauthorizedHandler = handler;
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  body?: unknown;
  /** Send the X-Perfil-Id header of the current session (default true). */
  withSession?: boolean;
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, withSession = true } = options;
  const destinoControlador = path.startsWith('/auth')
    ? 'AuthController.java'
    : path.startsWith('/perfiles')
      ? 'ProfileController.java'
      : 'MovieController.java';
  console.log(`[VENLY TRACE - FRONTEND] Acción: Procesando petición ${method} ${path} en frontend/src/services/httpClient.ts -> dirigiendo a backend/springboot backend/src/main/java/com/venly/backend/controller/${destinoControlador}`);
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

/** Extracts a user-facing message and per-field errors from any thrown error. */
export function parseApiError(error: unknown, fallback: string): { message: string; fields: Record<string, string> } {
  if (error instanceof HttpError) {
    return { message: error.payload?.mensaje ?? error.message ?? fallback, fields: error.payload?.errores ?? {} };
  }
  return { message: fallback, fields: {} };
}
