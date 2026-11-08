<script setup lang="ts">
import { reactive, ref } from 'vue';
import { getProfileById } from '@/services/profileService';
import { HttpError } from '@/services/httpClient';
import type { Profile } from '@/types/profile';

const form = reactive({
  perfilId: '',
  solicitanteId: ''
});

const loading = ref(false);
const profile = ref<Profile | null>(null);
const errorMessage = ref('');

async function consultProfile() {
  errorMessage.value = '';
  profile.value = null;

  if (!form.perfilId.trim() || !form.solicitanteId.trim()) {
    errorMessage.value = 'Debe indicar el ID del perfil y el X-Perfil-Id.';
    return;
  }

  loading.value = true;
  try {
    profile.value = await getProfileById(Number(form.perfilId), Number(form.solicitanteId));
  } catch (error) {
    if (error instanceof HttpError) {
      errorMessage.value = error.payload?.mensaje ?? 'No se pudo consultar el perfil.';
    } else {
      errorMessage.value = 'Error inesperado al consultar el perfil.';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="max-w-3xl mx-auto px-4 py-8">
    <section class="bg-white rounded-xl shadow p-6 space-y-5">
      <h2 class="text-2xl font-semibold">Consultar perfil</h2>
      <p class="text-sm text-slate-500">Este formulario usa la cabecera X-Perfil-Id para consultar el perfil propio.</p>

      <form class="grid grid-cols-1 md:grid-cols-2 gap-4" @submit.prevent="consultProfile">
        <label class="space-y-1">
          <span class="text-sm font-medium">ID del perfil</span>
          <input v-model="form.perfilId" type="number" min="1" class="w-full rounded-md border-slate-300" />
        </label>

        <label class="space-y-1">
          <span class="text-sm font-medium">X-Perfil-Id</span>
          <input v-model="form.solicitanteId" type="number" min="1" class="w-full rounded-md border-slate-300" />
        </label>

        <div class="md:col-span-2 flex items-center gap-3">
          <button
            type="submit"
            :disabled="loading"
            class="rounded-md bg-venly-primary px-4 py-2 text-white font-medium disabled:opacity-60"
          >
            {{ loading ? 'Consultando...' : 'Consultar perfil' }}
          </button>
          <button
            type="button"
            disabled
            class="rounded-md bg-slate-300 px-4 py-2 text-slate-600 font-medium cursor-not-allowed"
          >
            Editar perfil
          </button>
        </div>
      </form>

      <p v-if="errorMessage" class="text-red-700 text-sm">{{ errorMessage }}</p>

      <article v-if="profile" class="rounded-lg border border-slate-200 p-4 bg-slate-50">
        <h3 class="font-semibold text-lg mb-3">Perfil encontrado</h3>
        <ul class="space-y-1 text-sm">
          <li><strong>ID:</strong> {{ profile.id }}</li>
          <li><strong>Nombre:</strong> {{ profile.nombre }}</li>
          <li><strong>Cédula:</strong> {{ profile.cedula }}</li>
          <li><strong>Correo:</strong> {{ profile.correo }}</li>
          <li><strong>Teléfono:</strong> {{ profile.telefono }}</li>
          <li><strong>Rol:</strong> {{ profile.rol }}</li>
        </ul>
      </article>
    </section>
  </main>
</template>
