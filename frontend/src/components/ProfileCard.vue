<script setup lang="ts">
import { computed } from 'vue';
import RoleBadge from '@/components/RoleBadge.vue';
import type { Profile } from '@/types/profile';

const props = defineProps<{ profile: Profile; compact?: boolean }>();

const initials = computed(() =>
  props.profile.nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
);

const details = computed(() => [
  { label: 'ID de perfil', value: `#${props.profile.id}`, icon: '🎟️' },
  { label: 'Cédula', value: props.profile.cedula, icon: '🪪' },
  { label: 'Correo electrónico', value: props.profile.correo, icon: '✉️' },
  { label: 'Teléfono', value: props.profile.telefono, icon: '📞' }
]);
</script>

<template>
  <article :class="compact ? '' : 'venly-card overflow-hidden'">
    <div v-if="!compact" class="h-28 bg-gradient-to-r from-venly-accent/70 via-venly-card to-venly-gold/50"></div>
    <div :class="compact ? '' : 'px-6 pb-6 sm:px-8'">
      <div class="flex flex-col items-start gap-4 sm:flex-row sm:items-end sm:justify-between" :class="compact ? '' : '-mt-12'">
        <div class="flex items-end gap-4">
          <div
            class="flex h-24 w-24 shrink-0 items-center justify-center rounded-full border-4 border-venly-surface bg-gradient-to-br from-venly-accent to-venly-gold text-3xl font-extrabold text-white shadow-glow"
            aria-hidden="true"
          >
            {{ initials }}
          </div>
          <div class="pb-1">
            <h2 class="text-2xl font-bold">{{ profile.nombre }}</h2>
            <RoleBadge class="mt-1" :role="profile.rol" />
          </div>
        </div>
        <slot name="actions" />
      </div>

      <dl class="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div v-for="item in details" :key="item.label" class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
          <dt class="flex items-center gap-1.5 text-xs uppercase tracking-wider text-venly-muted">
            <span aria-hidden="true">{{ item.icon }}</span>{{ item.label }}
          </dt>
          <dd class="mt-1 break-all font-medium">{{ item.value }}</dd>
        </div>
      </dl>
    </div>
  </article>
</template>
