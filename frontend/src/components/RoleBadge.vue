<script setup lang="ts">
import type { Role } from '@/types/profile';

defineProps<{ role: Role; size?: 'sm' | 'md' }>();

const roleStyles: Record<Role, { label: string; classes: string }> = {
  CLIENTE: { label: 'Cliente', classes: 'bg-sky-500/15 text-sky-300 ring-sky-400/40' },
  GERENTE: { label: 'Gerente', classes: 'bg-venly-gold/15 text-venly-gold ring-venly-gold/40' },
  JEFE: { label: 'Jefe', classes: 'bg-venly-accent/15 text-rose-300 ring-venly-accent/40' }
};
</script>

<template>
  <span
    class="inline-flex items-center rounded-full font-semibold ring-1"
    :class="[roleStyles[role].classes, size === 'sm' ? 'px-2 py-0.5 text-[11px]' : 'px-3 py-0.5 text-xs']"
  >
    {{ roleStyles[role].label }}
  </span>
</template>
