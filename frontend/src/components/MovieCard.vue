<script setup lang="ts">
import { computed } from 'vue';
import { movieImageSrc } from '@/services/movieService';
import type { Movie } from '@/types/movie';

const props = defineProps<{ movie: Movie; editable?: boolean }>();
const emit = defineEmits<{ open: [id: number]; edit: [id: number] }>();

const imageSrc = computed(() => movieImageSrc(props.movie));
</script>

<template>
  <article
    class="group venly-card flex cursor-pointer flex-col overflow-hidden hover:-translate-y-1 hover:border-venly-accent/60 hover:shadow-glow focus-within:ring-2 focus-within:ring-venly-accent/50"
    @click="emit('open', movie.id)"
  >
    <div class="relative aspect-[2/3] overflow-hidden bg-gradient-to-br from-venly-card via-venly-surface to-venly-bg">
      <img
        v-if="imageSrc"
        :src="imageSrc"
        :alt="`Póster de ${movie.titulo}`"
        class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
        loading="lazy"
      />
      <div v-else class="flex h-full flex-col items-center justify-center gap-2 text-venly-muted">
        <span class="text-5xl opacity-70">🎬</span>
        <span class="text-xs uppercase tracking-widest">Sin póster</span>
      </div>
      <span class="absolute left-3 top-3 rounded-md bg-black/75 px-2 py-0.5 text-xs font-bold text-venly-gold ring-1 ring-venly-gold/50">
        {{ movie.clasificacion }}
      </span>
      <div class="absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-black/90 to-transparent"></div>
    </div>
    <div class="flex flex-1 flex-col gap-2 p-4">
      <h3 class="line-clamp-2 text-base font-bold leading-snug group-hover:text-white">{{ movie.titulo }}</h3>
      <div class="flex flex-wrap gap-x-3 gap-y-1 text-xs text-venly-muted">
        <span>🕒 {{ movie.horario }}</span>
        <span>⏱️ {{ movie.duracion }}</span>
      </div>
      <p class="text-xs text-venly-muted">📍 {{ movie.salaNombre }}</p>
      <div class="mt-auto flex gap-2 pt-2">
        <button
          type="button"
          class="flex-1 rounded-lg border border-venly-border px-3 py-1.5 text-sm font-medium text-venly-text hover:border-venly-accent hover:text-venly-accent focus:outline-none focus:ring-2 focus:ring-venly-accent/50"
          @click.stop="emit('open', movie.id)"
        >
          Ver detalle
        </button>
        <button
          v-if="editable"
          type="button"
          class="rounded-lg bg-venly-gold/15 px-3 py-1.5 text-sm font-semibold text-venly-gold ring-1 ring-venly-gold/40 hover:bg-venly-gold/25 focus:outline-none focus:ring-2 focus:ring-venly-gold/60"
          @click.stop="emit('edit', movie.id)"
        >
          ✏️ Editar
        </button>
      </div>
    </div>
  </article>
</template>
