<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import MovieCard from '@/components/MovieCard.vue';
import { useSession } from '@/composables/useSession';
import { parseApiError } from '@/services/httpClient';
import { getMovies } from '@/services/movieService';
import type { Movie } from '@/types/movie';
import { normalizeText } from '@/utils/validators';

const router = useRouter();
const { isStaff } = useSession();

const movies = ref<Movie[]>([]);
const loading = ref(true);
const error = ref('');
const search = ref('');

const filtered = computed(() => {
  const term = normalizeText(search.value);
  return term ? movies.value.filter((m) => normalizeText(m.titulo).includes(term)) : movies.value;
});

async function load() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Solicitud de cartelera\nDe: MovieCatalogView.vue -> Hacia: movieService.ts");
  loading.value = true;
  error.value = '';
  try {
    movies.value = await getMovies();
  } catch (e) {
    error.value = parseApiError(e, 'No se pudo cargar la cartelera.').message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="mx-auto max-w-6xl animate-fade-up">
    <RouterLink to="/menu" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver al menú</RouterLink>
    <header class="mb-8 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">{{ isStaff ? 'Gestión de películas' : 'Cartelera' }}</p>
        <h1 class="text-3xl font-extrabold">Catálogo de películas</h1>
        <p class="mt-1 text-sm text-venly-muted">{{ movies.length }} {{ movies.length === 1 ? 'película' : 'películas' }} en cartelera</p>
      </div>
      <div class="flex flex-col gap-3 sm:flex-row">
        <div class="relative sm:w-72">
          <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-venly-muted">🔍</span>
          <input v-model="search" type="search" placeholder="Buscar por título" class="venly-input pl-10" aria-label="Buscar películas" />
        </div>
        <RouterLink v-if="isStaff" :to="{ name: 'pelicula-nueva' }" class="venly-btn-primary whitespace-nowrap">＋ Añadir película</RouterLink>
      </div>
    </header>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Cargando cartelera…</div>
    <div v-else-if="error" class="venly-alert-error flex items-center justify-between gap-4" role="alert">
      {{ error }}
      <button type="button" class="venly-btn-secondary py-1.5" @click="load">Reintentar</button>
    </div>
    <div v-else-if="movies.length === 0" class="venly-card p-12 text-center">
      <p class="text-5xl">🎬</p>
      <p class="mt-3 text-lg font-semibold">Aún no hay películas en cartelera</p>
      <p class="mt-1 text-sm text-venly-muted">{{ isStaff ? 'Añade la primera película para comenzar.' : 'Vuelve pronto para ver los estrenos.' }}</p>
      <RouterLink v-if="isStaff" :to="{ name: 'pelicula-nueva' }" class="venly-btn-primary mt-5">＋ Añadir película</RouterLink>
    </div>
    <div v-else-if="filtered.length === 0" class="venly-card p-12 text-center text-venly-muted">No hay películas que coincidan con «{{ search }}».</div>

    <div v-else class="grid grid-cols-1 gap-6 min-[420px]:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
      <MovieCard
        v-for="movie in filtered"
        :key="movie.id"
        :movie="movie"
        :editable="isStaff"
        @open="(id) => router.push({ name: 'pelicula-detalle', params: { id } })"
        @edit="(id) => router.push({ name: 'pelicula-editar', params: { id } })"
      />
    </div>
  </section>
</template>
