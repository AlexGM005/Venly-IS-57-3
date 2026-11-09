import { computed, ref } from 'vue';
import type { Profile, Role } from '@/types/profile';

const STORAGE_KEY = 'venly.session';
const ROLES: Role[] = ['CLIENTE', 'GERENTE', 'JEFE'];

function readStoredProfile(): Profile | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<Profile>;
    const valid = typeof parsed.id === 'number' && typeof parsed.nombre === 'string' && ROLES.includes(parsed.rol as Role);
    return valid ? (parsed as Profile) : null;
  } catch {
    return null;
  }
}

// Module-level state: one simulated session (the profile stored in localStorage) shared by the whole app.
const currentUser = ref<Profile | null>(readStoredProfile());

/** Id used for the X-Perfil-Id header; read by the HTTP client on every request. */
export function getSessionProfileId(): number | null {
  return currentUser.value?.id ?? null;
}

export function useSession() {
  const isLoggedIn = computed(() => currentUser.value !== null);
  const isStaff = computed(() => currentUser.value?.rol === 'GERENTE' || currentUser.value?.rol === 'JEFE');
  const firstName = computed(() => currentUser.value?.nombre.split(' ')[0] ?? '');

  /** Saves the profile returned by login/register (or an updated profile) as the active session. */
  function setSession(profile: Profile) {
    currentUser.value = profile;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(profile));
  }

  function logout() {
    currentUser.value = null;
    localStorage.removeItem(STORAGE_KEY);
  }

  return { currentUser, isLoggedIn, isStaff, firstName, setSession, logout };
}
