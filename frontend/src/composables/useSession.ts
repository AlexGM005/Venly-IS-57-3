import { computed, ref } from 'vue';
import { getProfileById } from '@/services/profileService';
import { clearSessionProfileId, getSessionProfileId, saveSessionProfileId } from '@/utils/session';
import type { Profile } from '@/types/profile';

// Module-level state: a single simulated session shared by the whole app.
const currentUser = ref<Profile | null>(null);

export function useSession() {
  const isLoggedIn = computed(() => currentUser.value !== null);
  const isStaff = computed(() => currentUser.value?.rol === 'GERENTE' || currentUser.value?.rol === 'JEFE');

  function login(profile: Profile) {
    currentUser.value = profile;
    saveSessionProfileId(profile.id);
  }

  function logout() {
    currentUser.value = null;
    clearSessionProfileId();
  }

  /** Restores the session from localStorage; clears it if the stored profile no longer exists. */
  async function restore(): Promise<boolean> {
    if (currentUser.value) return true;
    const storedId = getSessionProfileId();
    if (!storedId) return false;
    try {
      currentUser.value = await getProfileById(storedId, storedId);
      return true;
    } catch {
      logout();
      return false;
    }
  }

  return { currentUser, isLoggedIn, isStaff, login, logout, restore };
}
