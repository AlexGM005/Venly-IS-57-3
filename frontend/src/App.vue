<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue';
</script>

<template>
  <div class="flex min-h-screen flex-col bg-venly-bg text-venly-text">
    <AppHeader />
    <main class="flex-1 flex flex-col justify-center">
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
    <footer class="border-t border-venly-border/60 py-5 text-center text-xs text-venly-muted">
      © {{ new Date().getFullYear() }} Venly · Tu experiencia de cine
    </footer>
  </div>
</template>