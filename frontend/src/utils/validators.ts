// Mirrors the backend Bean Validation rules so errors appear before sending the request.
export const NAME_REGEX = /^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]+$/;
const CEDULA_REGEX = /^[0-9]{6,12}$/;
const PHONE_REGEX = /^[0-9]{10,15}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*[0-9]).+$/;

export function validateName(value: string): string | undefined {
  const name = value.trim();
  if (!name) return 'El nombre es obligatorio';
  if (name.length > 120) return 'El nombre no puede exceder 120 caracteres';
  if (!NAME_REGEX.test(name)) return 'El nombre solo puede contener letras y espacios';
  return undefined;
}

export function validateCedula(value: string): string | undefined {
  return CEDULA_REGEX.test(value.trim()) ? undefined : 'La cédula debe contener entre 6 y 12 dígitos';
}

export function validateEmail(value: string): string | undefined {
  if (!value.trim()) return 'El correo es obligatorio';
  return EMAIL_REGEX.test(value.trim()) ? undefined : 'El correo no tiene un formato válido';
}

export function validatePhone(value: string): string | undefined {
  return PHONE_REGEX.test(value.trim()) ? undefined : 'El teléfono debe contener entre 10 y 15 dígitos';
}

export function validatePassword(value: string): string | undefined {
  if (!value) return 'La contraseña es obligatoria';
  if (value.length < 6) return 'La contraseña debe tener al menos 6 caracteres';
  return PASSWORD_REGEX.test(value) ? undefined : 'La contraseña debe contener letras y números';
}

/** Removes undefined entries so the result can be used directly as an error map. */
export function compactErrors<K extends string>(errors: Record<K, string | undefined>): Partial<Record<K, string>> {
  return Object.fromEntries(Object.entries(errors).filter(([, v]) => v)) as Partial<Record<K, string>>;
}

/** Lower-cases and strips accents for accent-insensitive searches. */
export function normalizeText(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
}
