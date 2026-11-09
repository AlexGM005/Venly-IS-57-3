<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useSession } from '@/composables/useSession';

const router = useRouter();
const { currentUser, isLoggedIn, logout } = useSession();
const menuOpen = ref(false);

const links = computed(() =>
  isLoggedIn.value
    ? [
        { to: '/menu', label: 'Menú principal' },
        { to: '/perfil', label: 'Perfil' }
      ]
    : [{ to: '/crear-perfil', label: 'Crear perfil' }]
);

function handleLogout() {
  menuOpen.value = false;
  logout();
  router.push({ name: 'crear-perfil' });
}
</script>

<template>
  <header
    class="sticky top-0 z-30 border-b border-venly-border/70 bg-gradient-to-r from-venly-bg via-venly-surface to-venly-bg/95 backdrop-blur"
  >
    <div class="mx-auto flex max-w-6xl items-center justify-between px-4 py-3.5">
      <RouterLink to="/" class="group flex items-center gap-2" @click="menuOpen = false">
        <span class="text-2xl transition-transform duration-300 group-hover:rotate-12">🎬</span>
        <span class="bg-gradient-to-r from-venly-gold to-amber-200 bg-clip-text text-2xl font-extrabold tracking-tight text-transparent">
          Venly
        </span>
      </RouterLink>

      <nav class="hidden items-center gap-1 sm:flex">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="relative rounded-md px-4 py-2 text-sm font-medium text-venly-muted hover:text-venly-text hover:[text-shadow:0_0_12px_rgba(233,69,96,0.8)]"
          active-class="!text-venly-text after:absolute after:inset-x-3 after:-bottom-0.5 after:h-0.5 after:rounded-full after:bg-venly-accent after:shadow-glow"
        >
          {{ link.label }}
        </RouterLink>
        <template v-if="currentUser">
          <span class="ml-3 border-l border-venly-border pl-4 text-sm text-venly-muted">{{ currentUser.nombre }}</span>
          <button
            type="button"
            class="ml-2 rounded-md px-3 py-2 text-sm text-venly-muted hover:bg-venly-card hover:text-venly-accent"
            @click="handleLogout"
          >
            Cerrar sesión
          </button>
        </template>
      </nav>

      <button
        type="button"
        class="rounded-md p-2 text-venly-muted hover:bg-venly-card hover:text-venly-text focus:outline-none focus:ring-2 focus:ring-venly-accent/50 sm:hidden"
        :aria-expanded="menuOpen"
        aria-label="Abrir menú"
        @click="menuOpen = !menuOpen"
      >
        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path v-if="!menuOpen" stroke-linecap="round" d="M4 7h16M4 12h16M4 17h16" />
          <path v-else stroke-linecap="round" d="M6 6l12 12M18 6L6 18" />
        </svg>
      </button>
    </div>

    <Transition name="expand">
      <nav v-if="menuOpen" class="border-t border-venly-border/60 px-4 pb-3 sm:hidden">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="mt-2 block rounded-md px-3 py-2 text-sm text-venly-muted hover:bg-venly-card hover:text-venly-text"
          active-class="!bg-venly-card !text-venly-text border-l-2 border-venly-accent"
          @click="menuOpen = false"
        >
          {{ link.label }}
        </RouterLink>
        <button
          v-if="isLoggedIn"
          type="button"
          class="mt-2 block w-full rounded-md px-3 py-2 text-left text-sm text-venly-muted hover:bg-venly-card hover:text-venly-accent"
          @click="handleLogout"
        >
          Cerrar sesión
        </button>
      </nav>
    </Transition>
  </header>
</template>
