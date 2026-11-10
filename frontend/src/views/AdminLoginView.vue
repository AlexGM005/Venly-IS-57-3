<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const correo = ref('');
const password = ref('');
const loading = ref(false);

const handleAdminLogin = async () => {
  loading.value = true;
  console.log('Iniciando sesión administrativo:', { correo: correo.value, password: password.value });
  // Tras autenticarse como GERENTE/JEFE, redirigir al menú o dashboard:
  router.push('/menu');
  loading.value = false;
};
</script>

<template>
  <div class="min-h-screen bg-venly-bg text-white flex flex-col justify-between relative">
    <main class="flex-1 flex items-center justify-center p-4">
      <div class="w-full max-w-md space-y-6">
        <div class="text-center space-y-2">
          <div class="inline-flex h-14 w-14 items-center justify-center rounded-full bg-venly-gold/10 border border-venly-gold/30 text-2xl text-venly-gold mb-1">
            🛡️
          </div>
          <p class="text-xs font-bold tracking-widest text-venly-gold uppercase">PORTAL ADMINISTRATIVO</p>
          <h2 class="text-3xl font-extrabold tracking-tight">Acceso Gerencial</h2>
          <p class="text-sm text-venly-muted">Exclusivo para Gerentes y Jefes de operación.</p>
        </div>

        <div class="venly-card p-6 sm:p-8 rounded-2xl border border-venly-gold/40 bg-venly-card/60 shadow-2xl space-y-5">
          <form @submit.prevent="handleAdminLogin" class="space-y-4">
            <div>
              <label class="block text-xs font-medium text-venly-muted mb-1">Correo corporativo / Usuario</label>
              <input
                v-model="correo"
                type="email"
                required
                placeholder="gerente@venly.com"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-gold outline-none transition-all"
              />
            </div>

            <div>
              <label class="block text-xs font-medium text-venly-muted mb-1">Contraseña</label>
              <input
                v-model="password"
                type="password"
                required
                placeholder="••••••••"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-gold outline-none transition-all"
              />
            </div>

            <button
              type="submit"
              :disabled="loading"
              class="w-full py-3 rounded-xl font-bold bg-venly-gold text-black hover:bg-yellow-400 active:scale-[0.98] transition-all shadow-glow"
            >
              {{ loading ? 'Validando...' : 'Ingresar al sistema' }}
            </button>
          </form>

          <div class="text-center text-xs text-venly-muted pt-3 border-t border-venly-border/40">
            <router-link to="/login" class="hover:text-white transition-colors">
              ← Volver al login de clientes
            </router-link>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>