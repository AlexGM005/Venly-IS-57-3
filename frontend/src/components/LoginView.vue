<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const correo = ref('');
const password = ref('');
const loading = ref(false);

const handleLogin = async () => {
  loading.value = true;
  console.log('Iniciando sesión cliente:', { correo: correo.value, password: password.value });
  // Tras validar sesión exitosa en el backend, redirigir al menú principal:
  router.push('/menu');
  loading.value = false;
};
</script>

<template>
  <div class="min-h-screen bg-venly-bg text-white flex flex-col justify-between relative transition-colors duration-300">
    <main class="flex-1 flex items-center justify-center p-4">
      <div class="w-full max-w-md space-y-6">
        <div class="text-center space-y-2">
          <p class="text-xs font-bold tracking-widest text-venly-gold uppercase">BIENVENIDO DE NUEVO</p>
          <h2 class="text-3xl font-extrabold tracking-tight">Inicia sesión en Venly</h2>
          <p class="text-sm text-venly-muted">Ingresa tus datos para acceder a tu cuenta.</p>
        </div>

        <div class="venly-card p-6 sm:p-8 rounded-2xl border border-venly-border bg-venly-card/40 shadow-xl space-y-5">
          <form @submit.prevent="handleLogin" class="space-y-4">
            <div>
              <label class="block text-xs font-medium text-venly-muted mb-1">Correo electrónico</label>
              <input
                v-model="correo"
                type="email"
                required
                placeholder="tu@correo.com"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-accent outline-none transition-all"
              />
            </div>

            <div>
              <label class="block text-xs font-medium text-venly-muted mb-1">Contraseña</label>
              <input
                v-model="password"
                type="password"
                required
                placeholder="••••••••"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-accent outline-none transition-all"
              />
            </div>

            <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 rounded-xl font-bold bg-venly-accent text-white hover:opacity-90 active:scale-[0.98] transition-all shadow-glow"
            >
              {{ loading ? 'Iniciando...' : 'Iniciar sesión' }}
            </button>
          </form>

          <div class="text-center text-xs text-venly-muted pt-3 border-t border-venly-border/40">
            ¿No tienes cuenta aún?
            <router-link to="/crear-perfil" class="text-venly-gold font-semibold hover:underline ml-1">
              Crea tu perfil aquí
            </router-link>
          </div>
        </div>
      </div>
    </main>

    <!-- Botón circular flotante en la esquina inferior derecha para Administradores/Gerentes -->
    <!-- <div class="fixed bottom-6 right-6 z-40">
      <router-link
        to="/admin-login"
        class="flex h-12 w-12 items-center justify-center rounded-full bg-venly-card border border-venly-gold/40 text-venly-gold shadow-2xl hover:scale-110 hover:border-venly-gold hover:bg-venly-gold/10 transition-all group"
        title="Acceso exclusivo para Gerentes"
      >
        <span class="text-xl group-hover:rotate-12 transition-transform">🛡️</span>
      </router-link>
    </div> -->
  </div>
</template>