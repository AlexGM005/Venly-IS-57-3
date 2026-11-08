<script setup lang="ts">
import { computed } from 'vue';
import type { Profile, Role } from '@/types/profile';

const props = defineProps<{ profile: Profile }>();

const roleStyles: Record<Role, { label: string; classes: string }> = {
  CLIENTE: { label: 'Cliente', classes: 'bg-sky-500/15 text-sky-300 ring-sky-400/40' },
  GERENTE: { label: 'Gerente', classes: 'bg-venly-gold/15 text-venly-gold ring-venly-gold/40' },
  JEFE: { label: 'Jefe', classes: 'bg-venly-accent/15 text-rose-300 ring-venly-accent/40' }
};

const initials = computed(() =>
  props.profile.nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
);

const details = computed(() => [
  { label: 'ID de perfil', value: `#${props.profile.id}` },
  { label: 'Cédula', value: props.profile.cedula },
  { label: 'Correo electrónico', value: props.profile.correo },
  { label: 'Teléfono', value: props.profile.telefono }
]);
</script>

<template>
  <article class="venly-card overflow-hidden">
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
        <slot name="actions" />
      </div>

      <dl class="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div v-for="item in details" :key="item.label" class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
          <dt class="text-xs uppercase tracking-wider text-venly-muted">{{ item.label }}</dt>
          <dd class="mt-1 break-all font-medium">{{ item.value }}</dd>
        </div>
      </dl>
    </div>
  </article>
</template>
