<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue';
</script>

<template>
  <div class="flex min-h-screen flex-col bg-transparent text-venly-text">
    <AppHeader />
    <main class="flex-1 px-4 py-8 sm:py-12">
      <RouterView v-slot="{ Component, route }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </RouterView>
    </main>
    <footer class="border-t border-venly-border/60 py-5 text-center text-xs text-venly-muted">
      © {{ new Date().getFullYear() }} Venly · Tu experiencia de cine
    </footer>
  </div>
</template>
