<script setup lang="ts">
import { onBeforeUnmount, watch } from 'vue';

const props = defineProps<{ open: boolean; title: string }>();
const emit = defineEmits<{ close: [] }>();

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') emit('close');
}

watch(
  () => props.open,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : '';
    if (open) window.addEventListener('keydown', onKeydown);
    else window.removeEventListener('keydown', onKeydown);
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  document.body.style.overflow = '';
  window.removeEventListener('keydown', onKeydown);
});
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="fixed inset-0 z-50 flex items-end justify-center p-0 sm:items-center sm:p-4" role="dialog" aria-modal="true" :aria-label="title">
        <div class="absolute inset-0 bg-black/70 backdrop-blur-sm" @click="emit('close')"></div>
        <div class="modal-panel venly-card relative max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-b-none sm:rounded-2xl">
          <div class="sticky top-0 z-10 flex items-center justify-between border-b border-venly-border bg-venly-surface/95 px-6 py-4 backdrop-blur">
            <h2 class="text-lg font-bold">{{ title }}</h2>
            <button
              type="button"
              class="rounded-md p-1.5 text-venly-muted hover:bg-venly-card hover:text-venly-text focus:outline-none focus:ring-2 focus:ring-venly-accent/50"
              aria-label="Cerrar"
              @click="emit('close')"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" d="M6 6l12 12M18 6L6 18" />
              </svg>
            </button>
          </div>
          <div class="p-6">
            <slot />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
