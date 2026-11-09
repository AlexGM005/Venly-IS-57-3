<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import RoleBadge from '@/components/RoleBadge.vue';
import { useSession } from '@/composables/useSession';

const router = useRouter();
const { currentUser, isLoggedIn, isStaff, logout } = useSession();
const menuOpen = ref(false);

const links = computed(() => {
  if (!isLoggedIn.value) return [];
  return isStaff.value
    ? [
        { to: '/menu', label: 'Menú' },
        { to: '/perfiles', label: 'Gestión de perfiles' },
        { to: '/peliculas', label: 'Gestión de películas' },
        { to: '/perfil', label: 'Mi perfil' }
      ]
    : [
        { to: '/menu', label: 'Menú' },
        { to: '/peliculas', label: 'Cartelera' },
        { to: '/perfil', label: 'Mi perfil' }
      ];
});

function handleLogout() {
  menuOpen.value = false;
  logout();
  router.push({ name: 'login' });
}
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-venly-border/70 bg-gradient-to-r from-venly-bg via-venly-surface to-venly-bg/95 backdrop-blur">
    <div class="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
      <RouterLink :to="isLoggedIn ? '/menu' : '/login'" class="group flex items-center gap-2" @click="menuOpen = false">
        <span class="text-2xl transition-transform duration-300 group-hover:rotate-12">🎬</span>
        <span class="bg-gradient-to-r from-venly-gold to-amber-200 bg-clip-text text-2xl font-extrabold tracking-tight text-transparent">Venly</span>
      </RouterLink>

      <nav v-if="isLoggedIn" class="hidden items-center gap-1 lg:flex">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="relative rounded-md px-3 py-2 text-sm font-medium text-venly-muted hover:text-venly-text"
          active-class="!text-venly-text after:absolute after:inset-x-3 after:-bottom-0.5 after:h-0.5 after:rounded-full after:bg-venly-accent after:shadow-glow"
        >
          {{ link.label }}
        </RouterLink>
      </nav>

      <div v-if="currentUser" class="hidden items-center gap-3 lg:flex">
        <div class="flex items-center gap-2 border-l border-venly-border pl-4">
          <span class="max-w-[10rem] truncate text-sm font-medium">{{ currentUser.nombre }}</span>
          <RoleBadge :role="currentUser.rol" size="sm" />
        </div>
        <button
          type="button"
          class="rounded-md border border-venly-border px-3 py-1.5 text-sm text-venly-muted hover:border-venly-accent hover:text-venly-accent focus:outline-none focus:ring-2 focus:ring-venly-accent/50"
          @click="handleLogout"
        >
          Cerrar sesión
        </button>
      </div>

      <button
        v-if="isLoggedIn"
        type="button"
        class="rounded-md p-2 text-venly-muted hover:bg-venly-card hover:text-venly-text focus:outline-none focus:ring-2 focus:ring-venly-accent/50 lg:hidden"
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
      <nav v-if="menuOpen && currentUser" class="border-t border-venly-border/60 px-4 pb-3 lg:hidden">
        <div class="flex items-center gap-2 py-3">
          <span class="text-sm font-medium">{{ currentUser.nombre }}</span>
          <RoleBadge :role="currentUser.rol" size="sm" />
        </div>
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="mt-1 block rounded-md px-3 py-2 text-sm text-venly-muted hover:bg-venly-card hover:text-venly-text"
          active-class="!bg-venly-card !text-venly-text border-l-2 border-venly-accent"
          @click="menuOpen = false"
        >
          {{ link.label }}
        </RouterLink>
        <button
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
