<script setup lang="ts">
import { computed } from 'vue';
import { useSession } from '@/composables/useSession';

const { currentUser, isStaff } = useSession();

const firstName = computed(() => currentUser.value?.nombre.trim().split(/\s+/)[0] ?? '');

const profileOptions = computed(() => [
  {
    to: '/perfil',
    icon: isStaff.value ? '🔎' : '👤',
    title: 'Consultar perfil',
    description: isStaff.value
      ? 'Consulta tu perfil o el de cualquier usuario por su ID.'
      : 'Revisa los datos de tu cuenta.'
  }
]);
</script>

<template>
  <section class="mx-auto max-w-5xl animate-fade-up">
    <header class="mb-10">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Menú principal</p>
      <h1 class="text-3xl font-extrabold sm:text-4xl">¡Hola, {{ firstName }}! 🍿</h1>
      <p class="mt-2 text-venly-muted">¿Qué quieres hacer hoy?</p>
    </header>

    <div class="venly-card p-6 sm:p-8">
      <h2 class="mb-5 flex items-center gap-2 text-lg font-bold">
        <span class="h-5 w-1 rounded-full bg-venly-accent"></span>
        Perfil
      </h2>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <RouterLink
          v-for="option in profileOptions"
          :key="option.to"
          :to="option.to"
          class="group rounded-xl border border-venly-border bg-venly-bg/50 p-5 hover:-translate-y-1 hover:border-venly-accent/70 hover:shadow-glow focus:outline-none focus:ring-2 focus:ring-venly-accent/50"
        >
          <span class="text-3xl">{{ option.icon }}</span>
          <h3 class="mt-3 font-semibold group-hover:text-white">{{ option.title }}</h3>
          <p class="mt-1 text-sm text-venly-muted">{{ option.description }}</p>
          <span class="mt-4 inline-block text-sm font-medium text-venly-accent">Entrar →</span>
        </RouterLink>
      </div>
    </div>
  </section>
</template>
