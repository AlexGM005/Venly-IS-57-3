<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { getProfileById } from '@/services/profileService';
import { HttpError } from '@/services/httpClient';
import { clearSessionProfileId, getSessionProfileId, saveSessionProfileId } from '@/utils/session';
import type { Profile, Role } from '@/types/profile';

const profileIdInput = ref('');
const loading = ref(false);
const profile = ref<Profile | null>(null);
const errorMessage = ref('');

const roleStyles: Record<Role, { label: string; classes: string }> = {
  CLIENTE: { label: 'Cliente', classes: 'bg-sky-500/15 text-sky-300 ring-sky-400/40' },
  GERENTE: { label: 'Gerente', classes: 'bg-venly-gold/15 text-venly-gold ring-venly-gold/40' },
  JEFE: { label: 'Jefe', classes: 'bg-venly-accent/15 text-rose-300 ring-venly-accent/40' }
};

const initials = computed(() => {
  if (!profile.value) return '';
  return profile.value.nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('');
});

const details = computed(() =>
  profile.value
    ? [
        { label: 'ID de perfil', value: `#${profile.value.id}` },
        { label: 'Cédula', value: profile.value.cedula },
        { label: 'Correo electrónico', value: profile.value.correo },
        { label: 'Teléfono', value: profile.value.telefono }
      ]
    : []
);

async function loadProfile(id: number) {
  errorMessage.value = '';
  profile.value = null;
  loading.value = true;
  try {
    // Simulated session: the requester is the stored/entered profile itself (X-Perfil-Id = id).
    profile.value = await getProfileById(id, id);
    saveSessionProfileId(id);
  } catch (error) {
    errorMessage.value =
      error instanceof HttpError
        ? error.payload?.mensaje ?? 'No se pudo consultar el perfil.'
        : 'No se pudo conectar con el servidor. Verifique que el backend esté activo.';
  } finally {
    loading.value = false;
  }
}

function submitSearch() {
  const id = Number(profileIdInput.value);
  if (!Number.isInteger(id) || id <= 0) {
    errorMessage.value = 'Ingrese un ID de perfil válido (número entero positivo).';
    return;
  }
  loadProfile(id);
}

function logout() {
  clearSessionProfileId();
  profile.value = null;
  profileIdInput.value = '';
  errorMessage.value = '';
}

onMounted(() => {
  const storedId = getSessionProfileId();
  if (storedId) {
    profileIdInput.value = String(storedId);
    loadProfile(storedId);
  }
});
</script>

<template>
  <section class="mx-auto max-w-3xl animate-fade-up">
    <header class="mb-8 text-center">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Mi cuenta</p>
      <h1 class="text-3xl font-extrabold sm:text-4xl">Consultar perfil</h1>
      <p class="mt-2 text-venly-muted">Tu sesión se recuerda automáticamente tras crear tu perfil.</p>
    </header>

    <form class="venly-card mb-6 flex flex-col gap-3 p-5 sm:flex-row sm:items-end" @submit.prevent="submitSearch">
      <div class="flex-1">
        <label for="perfilId" class="venly-label">ID de perfil</label>
        <input id="perfilId" v-model="profileIdInput" type="number" min="1" placeholder="Ej. 1" class="venly-input" />
      </div>
      <button type="submit" :disabled="loading" class="venly-btn-primary">
        <LoadingSpinner v-if="loading" />
        {{ loading ? 'Consultando...' : 'Consultar' }}
      </button>
    </form>

    <Transition name="fade">
      <div v-if="errorMessage" class="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-sm text-red-300" role="alert">
        {{ errorMessage }}
      </div>
    </Transition>

    <div v-if="loading && !profile" class="venly-card flex animate-pulse items-center gap-5 p-8">
      <div class="h-20 w-20 rounded-full bg-venly-card"></div>
      <div class="flex-1 space-y-3">
        <div class="h-5 w-1/2 rounded bg-venly-card"></div>
        <div class="h-4 w-1/4 rounded bg-venly-card"></div>
      </div>
    </div>

    <Transition name="fade">
      <article v-if="profile" class="venly-card overflow-hidden">
        <div class="h-24 bg-gradient-to-r from-venly-accent/70 via-venly-card to-venly-gold/50"></div>
        <div class="px-6 pb-6 sm:px-8">
          <div class="-mt-12 flex flex-col items-start gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div class="flex items-end gap-4">
              <div
                class="flex h-24 w-24 shrink-0 items-center justify-center rounded-full border-4 border-venly-surface bg-gradient-to-br from-venly-accent to-venly-gold text-3xl font-extrabold text-white shadow-glow"
                aria-hidden="true"
              >
                {{ initials }}
              </div>
              <div class="pb-1">
                <h2 class="text-2xl font-bold">{{ profile.nombre }}</h2>
                <span
                  class="mt-1 inline-flex items-center rounded-full px-3 py-0.5 text-xs font-semibold ring-1"
                  :class="roleStyles[profile.rol].classes"
                >
                  {{ roleStyles[profile.rol].label }}
                </span>
              </div>
            </div>

            <div class="group relative">
              <button
                type="button"
                disabled
                aria-describedby="edit-tooltip"
                class="cursor-not-allowed rounded-lg border border-venly-border bg-venly-card px-4 py-2 text-sm font-medium text-venly-muted"
              >
                ✏️ Editar perfil
              </button>
              <span
                id="edit-tooltip"
                role="tooltip"
                class="pointer-events-none absolute -top-10 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-md bg-black px-3 py-1.5 text-xs text-venly-text opacity-0 shadow-lg transition-opacity duration-200 group-hover:opacity-100"
              >
                Disponible próximamente
              </span>
            </div>
          </div>

          <dl class="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div v-for="item in details" :key="item.label" class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
              <dt class="text-xs uppercase tracking-wider text-venly-muted">{{ item.label }}</dt>
              <dd class="mt-1 break-all font-medium">{{ item.value }}</dd>
            </div>
          </dl>

          <div class="mt-6 text-right">
            <button type="button" class="text-sm text-venly-muted underline-offset-4 hover:text-venly-accent hover:underline" @click="logout">
              Cerrar sesión
            </button>
          </div>
        </div>
      </article>
    </Transition>

    <div v-if="!profile && !loading && !errorMessage" class="venly-card p-8 text-center text-venly-muted">
      <p class="text-4xl">🎟️</p>
      <p class="mt-3">No hay una sesión activa. Ingresa tu ID o
        <RouterLink to="/crear-perfil" class="text-venly-accent hover:underline">crea un perfil</RouterLink>.
      </p>
    </div>
  </section>
</template>
