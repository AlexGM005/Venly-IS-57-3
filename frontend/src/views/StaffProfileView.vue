<script setup lang="ts">
import { ref } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import ProfileCard from '@/components/ProfileCard.vue';
import DisabledActionButton from '@/components/DisabledActionButton.vue';
import { getProfileById } from '@/services/profileService';
import { HttpError } from '@/services/httpClient';
import type { Profile } from '@/types/profile';

const props = defineProps<{ currentUser: Profile }>();

const profileIdInput = ref(String(props.currentUser.id));
const loading = ref(false);
const profile = ref<Profile | null>(props.currentUser);
const errorMessage = ref('');

async function submitSearch() {
  const id = Number(profileIdInput.value);
  if (!Number.isInteger(id) || id <= 0) {
    errorMessage.value = 'Ingrese un ID de perfil válido (número entero positivo).';
    return;
  }
  errorMessage.value = '';
  profile.value = null;
  loading.value = true;
  try {
    // The requester is always the logged-in staff member, not the searched profile.
    profile.value = await getProfileById(id, props.currentUser.id);
  } catch (error) {
    errorMessage.value =
      error instanceof HttpError
        ? error.payload?.mensaje ?? 'No se pudo consultar el perfil.'
        : 'No se pudo conectar con el servidor. Verifique que el backend esté activo.';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div>
    <header class="mb-8 text-center">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Administración</p>
      <h1 class="text-3xl font-extrabold sm:text-4xl">Consultar perfil</h1>
      <p class="mt-2 text-venly-muted">Busca cualquier perfil registrado por su ID.</p>
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

    <div v-if="loading" class="venly-card flex animate-pulse items-center gap-5 p-8">
      <div class="h-20 w-20 rounded-full bg-venly-card"></div>
      <div class="flex-1 space-y-3">
        <div class="h-5 w-1/2 rounded bg-venly-card"></div>
        <div class="h-4 w-1/4 rounded bg-venly-card"></div>
      </div>
    </div>

    <Transition name="fade">
      <ProfileCard v-if="profile" :profile="profile">
        <template #actions>
          <DisabledActionButton label="Editar perfil" />
        </template>
      </ProfileCard>
    </Transition>
  </div>
</template>
