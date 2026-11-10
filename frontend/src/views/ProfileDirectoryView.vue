<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import BaseModal from '@/components/BaseModal.vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import ProfileCard from '@/components/ProfileCard.vue';
import RoleBadge from '@/components/RoleBadge.vue';
import { parseApiError } from '@/services/httpClient';
import { getAllProfiles, getProfileById } from '@/services/profileService';
import type { Profile, Role } from '@/types/profile';
import { normalizeText } from '@/utils/validators';

const profiles = ref<Profile[]>([]);
const loading = ref(true);
const error = ref('');
const search = ref('');
const roleFilter = ref<Role | 'TODOS'>('TODOS');

const selected = ref<Profile | null>(null);
const detailOpen = ref(false);
const detailLoading = ref(false);
const detailError = ref('');

const roleFilters: { value: Role | 'TODOS'; label: string }[] = [
  { value: 'TODOS', label: 'Todos' },
  { value: 'CLIENTE', label: 'Clientes' },
  { value: 'GERENTE', label: 'Gerentes' },
  { value: 'JEFE', label: 'Jefes' }
];

const filtered = computed(() => {
  const term = normalizeText(search.value);
  return profiles.value.filter((p) => {
    const matchesRole = roleFilter.value === 'TODOS' || p.rol === roleFilter.value;
    const matchesText = !term || normalizeText(p.nombre).includes(term) || p.cedula.includes(term);
    return matchesRole && matchesText;
  });
});

function countBy(role: Role | 'TODOS') {
  return role === 'TODOS' ? profiles.value.length : profiles.value.filter((p) => p.rol === role).length;
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    profiles.value = await getAllProfiles();
  } catch (e) {
    error.value = parseApiError(e, 'No se pudo cargar el directorio de perfiles.').message;
  } finally {
    loading.value = false;
  }
}

async function openDetail(profile: Profile) {
  selected.value = profile;
  detailOpen.value = true;
  detailError.value = '';
  detailLoading.value = true;
  try {
    selected.value = await getProfileById(profile.id);
  } catch (e) {
    detailError.value = parseApiError(e, 'No se pudo cargar el detalle del perfil.').message;
  } finally {
    detailLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="mx-auto max-w-6xl animate-fade-up">
    <RouterLink to="/menu" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver al menú</RouterLink>
    <header class="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Gestión de perfiles</p>
        <h1 class="text-3xl font-extrabold">Directorio de perfiles</h1>
        <p class="mt-1 text-sm text-venly-muted">{{ profiles.length }} perfiles registrados</p>
      </div>
      <div class="relative w-full sm:w-80">
        <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-venly-muted">🔍</span>
        <input v-model="search" type="search" placeholder="Buscar por nombre o cédula" class="venly-input pl-10" aria-label="Buscar perfiles" />
      </div>
    </header>

    <div class="mb-5 flex flex-wrap gap-2">
      <button
        v-for="f in roleFilters"
        :key="f.value"
        type="button"
        class="rounded-full border px-3.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-venly-accent/50"
        :class="roleFilter === f.value ? 'border-venly-accent bg-venly-accent/15 text-venly-text' : 'border-venly-border text-venly-muted hover:text-venly-text'"
        @click="roleFilter = f.value"
      >
        {{ f.label }} <span class="ml-1 text-xs opacity-70">{{ countBy(f.value) }}</span>
      </button>
    </div>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted">
      <LoadingSpinner /> Cargando perfiles…
    </div>
    <div v-else-if="error" class="venly-alert-error flex items-center justify-between gap-4" role="alert">
      {{ error }}
      <button type="button" class="venly-btn-secondary py-1.5" @click="load">Reintentar</button>
    </div>
    <div v-else-if="filtered.length === 0" class="venly-card p-12 text-center text-venly-muted">
      <p class="text-4xl">🔎</p>
      <p class="mt-2">No hay perfiles que coincidan con la búsqueda.</p>
    </div>

    <template v-else>
      <!-- Desktop table -->
      <div class="venly-card hidden overflow-hidden md:block">
        <table class="w-full text-left text-sm">
          <thead class="border-b border-venly-border bg-venly-bg/50 text-xs uppercase tracking-wider text-venly-muted">
            <tr>
              <th class="px-5 py-3">ID</th>
              <th class="px-5 py-3">Nombre</th>
              <th class="px-5 py-3">Cédula</th>
              <th class="px-5 py-3">Correo</th>
              <th class="px-5 py-3">Teléfono</th>
              <th class="px-5 py-3">Rol</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="p in filtered"
              :key="p.id"
              tabindex="0"
              class="cursor-pointer border-b border-venly-border/50 last:border-0 hover:bg-venly-accent/5 focus:bg-venly-accent/10 focus:outline-none"
              @click="openDetail(p)"
              @keydown.enter="openDetail(p)"
            >
              <td class="px-5 py-3.5 text-venly-muted">#{{ p.id }}</td>
              <td class="px-5 py-3.5 font-medium">{{ p.nombre }}</td>
              <td class="px-5 py-3.5">{{ p.cedula }}</td>
              <td class="px-5 py-3.5 text-venly-muted">{{ p.correo }}</td>
              <td class="px-5 py-3.5 text-venly-muted">{{ p.telefono }}</td>
              <td class="px-5 py-3.5"><RoleBadge :role="p.rol" size="sm" /></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile cards -->
      <ul class="grid gap-3 md:hidden">
        <li v-for="p in filtered" :key="p.id">
          <button type="button" class="venly-card flex w-full items-center justify-between gap-3 p-4 text-left hover:border-venly-accent/60" @click="openDetail(p)">
            <div>
              <p class="font-semibold">{{ p.nombre }}</p>
              <p class="text-xs text-venly-muted">C.I. {{ p.cedula }} · #{{ p.id }}</p>
            </div>
            <RoleBadge :role="p.rol" size="sm" />
          </button>
        </li>
      </ul>
    </template>

    <BaseModal :open="detailOpen" title="Detalle del perfil" @close="detailOpen = false">
      <div v-if="detailError" class="venly-alert-error mb-4" role="alert">{{ detailError }}</div>
      <div v-if="detailLoading" class="mb-4 flex items-center gap-2 text-sm text-venly-muted"><LoadingSpinner /> Actualizando…</div>
      <ProfileCard v-if="selected" :profile="selected" compact />
    </BaseModal>
  </section>
</template>
