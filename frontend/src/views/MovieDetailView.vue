<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useSession } from '@/composables/useSession';
import { parseApiError } from '@/services/httpClient';
import { getMovie, movieImageSrc } from '@/services/movieService';
import type { Movie } from '@/types/movie';

const props = defineProps<{ id: number }>();
const { isStaff } = useSession();

const movie = ref<Movie | null>(null);
const loading = ref(true);
const error = ref('');
const imageSrc = computed(() => (movie.value ? movieImageSrc(movie.value) : null));

onMounted(async () => {
  console.log("[VENLY TRACE - FRONTEND] Acción: Solicitando detalle de película seleccionada en frontend/src/views/MovieDetailView.vue -> enviando petición a frontend/src/services/movieService.ts");
  try {
    movie.value = await getMovie(props.id);
  } catch (e) {
    error.value = parseApiError(e, 'No se pudo cargar la película.').message;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="mx-auto max-w-5xl animate-fade-up">
    <RouterLink to="/peliculas" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver al catálogo</RouterLink>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Cargando película…</div>
    <div v-else-if="error" class="venly-alert-error" role="alert">{{ error }}</div>

    <article v-else-if="movie" class="venly-card overflow-hidden md:grid md:grid-cols-[minmax(0,320px)_1fr]">
      <div class="relative aspect-[2/3] bg-gradient-to-br from-venly-card via-venly-surface to-venly-bg">
        <img v-if="imageSrc" :src="imageSrc" :alt="`Póster de ${movie.titulo}`" class="h-full w-full object-cover" />
        <div v-else class="flex h-full flex-col items-center justify-center gap-2 text-venly-muted">
          <span class="text-6xl opacity-70">🎬</span>
          <span class="text-xs uppercase tracking-widest">Sin póster</span>
        </div>
      </div>
      <div class="flex flex-col p-6 sm:p-8">
        <div class="flex flex-wrap items-center gap-2">
          <span class="rounded-md bg-black/60 px-2.5 py-0.5 text-sm font-bold text-venly-gold ring-1 ring-venly-gold/50">{{ movie.clasificacion }}</span>
          <span class="text-sm text-venly-muted">Película #{{ movie.id }}</span>
        </div>
        <h1 class="mt-3 text-3xl font-extrabold sm:text-4xl">{{ movie.titulo }}</h1>

        <dl class="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">🕒 Horario</dt>
            <dd class="mt-1 font-semibold">{{ movie.horario }}</dd>
          </div>
          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">⏱️ Duración</dt>
            <dd class="mt-1 font-semibold">{{ movie.duracion }}</dd>
          </div>
          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">📍 Sala</dt>
            <dd class="mt-1 font-semibold">{{ movie.salaNombre }}</dd>
          </div>
        </dl>

        <h2 class="mt-8 text-sm font-semibold uppercase tracking-wider text-venly-gold">Sinopsis</h2>
        <p class="mt-2 whitespace-pre-line leading-relaxed text-gray-300">{{ movie.sinopsis }}</p>

        <!-- <div v-if="isStaff" class="mt-auto pt-8">
          <RouterLink :to="{ name: 'pelicula-editar', params: { id: movie.id } }" class="venly-btn-primary">✏️ Editar película</RouterLink>
        </div> -->
      </div>
    </article>
  </section>
</template>
