<script setup lang="ts">
defineProps<{ label: string }>();
</script>

<template>
  <div class="group relative">
    <button
      type="button"
      disabled
      class="cursor-not-allowed rounded-lg border border-venly-border bg-venly-card px-4 py-2 text-sm font-medium text-venly-muted"
    >
      ✏️ {{ label }}
    </button>
    <span
      role="tooltip"
      class="pointer-events-none absolute -top-10 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-md bg-black px-3 py-1.5 text-xs text-venly-text opacity-0 shadow-lg transition-opacity duration-200 group-hover:opacity-100"
    >
      Disponible próximamente
    </span>
  </div>
</template>
