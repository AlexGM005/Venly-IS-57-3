<script setup lang="ts">
import { computed } from 'vue';
import RoleBadge from '@/components/RoleBadge.vue';
import { useSession } from '@/composables/useSession';
import RegistrarGerenteView from '@/views/RegistrarGerenteView.vue';

const { currentUser, firstName, isStaff } = useSession();

const isJefe = computed(() => currentUser.value?.rol === 'JEFE');

interface MenuOption {
  to: string;
  title: string;
  description: string;
  icon: string;
  accent: string;
}

const options = computed<MenuOption[]>(() => {
  if (isStaff.value) {
    const staffOptions: MenuOption[] = [
      {
        to: '/perfiles',
        title: 'Gestión de Perfiles',
        description: 'Directorio de todos los perfiles registrados, con búsqueda por nombre o cédula.',
        icon: '👥',
        accent: 'from-venly-gold/30'
      },
      {
        to: '/peliculas',
        title: 'Gestión de Películas',
        description: 'Añade nuevas películas a la cartelera y edita las existentes.',
        icon: '🎞️',
        accent: 'from-venly-accent/30'
      }
    ];

    // Solo se agrega esta tarjeta si el usuario tiene el rol JEFE
    if (isJefe.value) {
      staffOptions.push({
        to: '/registro-gerente', // 👈 Ajusta esta ruta a tu ruta real de registro
        title: 'Registrar Gerente',
        description: 'Crear nuevas cuentas administrativas con acceso a gestión de salas y películas.',
        icon: '👔',
        accent: 'from-emerald-500/30' // Destacado en tono esmeralda/verde
      });
    }

    return staffOptions;
  }

  return [
    {
      to: '/perfil',
      title: 'Mi Perfil',
      description: 'Consulta tus datos y actualiza tu nombre, correo o teléfono.',
      icon: '👤',
      accent: 'from-sky-500/30'
    },
    {
      to: '/peliculas',
      title: 'Catálogo de Películas',
      description: 'Explora la cartelera con horarios, duración, clasificación y sala.',
      icon: '🍿',
      accent: 'from-venly-accent/30'
    }
  ];
});
</script>

<template>
  <section v-if="currentUser" class="mx-auto max-w-5xl animate-fade-up">
    <header class="mb-10">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Menú principal</p>
      <h1 class="text-3xl font-extrabold sm:text-4xl">¡Hola, {{ firstName }}! 🍿</h1>
      <p class="mt-2 flex flex-wrap items-center gap-2 text-venly-muted">
        Has iniciado sesión como <RoleBadge :role="currentUser.rol" />
      </p>
    </header>
  

    <div class="grid gap-6 sm:grid-cols-2">
      <RouterLink
        v-for="option in options"
        :key="option.to"
        :to="option.to"
        class="group venly-card relative overflow-hidden p-7 hover:-translate-y-1 hover:border-venly-accent/60 hover:shadow-glow focus:outline-none focus:ring-2 focus:ring-venly-accent/60"
      >
        <div class="pointer-events-none absolute inset-0 bg-gradient-to-br to-transparent opacity-60 transition-opacity group-hover:opacity-100" :class="option.accent"></div>
        <div class="relative">
          <span class="flex h-14 w-14 items-center justify-center rounded-2xl bg-venly-bg/70 text-3xl ring-1 ring-venly-border transition-transform duration-300 group-hover:scale-110">
            {{ option.icon }}
          </span>
          <h2 class="mt-5 text-xl font-bold">{{ option.title }}</h2>
          <p class="mt-2 text-sm text-venly-muted">{{ option.description }}</p>
          <span class="mt-5 inline-flex items-center gap-1 text-sm font-semibold text-venly-accent">
            Entrar <span class="transition-transform group-hover:translate-x-1">→</span>
          </span>
        </div>
      </RouterLink>
    </div>
  </section>
</template>
