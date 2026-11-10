<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { createProfile } from '@/services/profileService';

const router = useRouter();

const nombre = ref('');
const cedula = ref('');
const telefono = ref('');
const correo = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');

const handleCreateProfile = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';

    await createProfile({
      nombre: nombre.value,
      cedula: cedula.value,
      telefono: telefono.value,
      correo: correo.value,
      password: password.value,
      rol: 'CLIENTE'
    });

    router.push('/login');
  } catch (error: any) {
    errorMessage.value = error?.response?.data?.mensaje || 'Error al crear el perfil';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="w-full flex flex-col items-center justify-center py-4">
    <div class="w-full max-w-xl space-y-6">
      
      <!-- Encabezado con botón de regreso opcional en la esquina -->
      <div class="text-center space-y-2 relative">
        <button
          type="button"
          @click="router.push('/login')"
          class="sm:absolute sm:left-0 sm:top-1/2 sm:-translate-y-1/2 inline-flex items-center gap-1.5 text-xs text-venly-muted hover:text-venly-gold transition-colors mb-2 sm:mb-0"
        >
          ← Volver al login
        </button>
        <p class="text-xs font-bold tracking-widest text-venly-gold uppercase">REGISTRO</p>
        <h2 class="text-3xl font-extrabold tracking-tight">Crea tu perfil en Venly</h2>
        <p class="text-sm text-venly-muted">Únete y vive la mejor experiencia de cine.</p>
      </div>

      <div class="venly-card p-6 sm:p-8 rounded-2xl border border-venly-border bg-venly-card/40 shadow-xl space-y-5">
        <p v-if="errorMessage" class="p-3 rounded-lg bg-rose-500/10 border border-rose-500/40 text-rose-300 text-xs text-center">
          {{ errorMessage }}
        </p>

        <form @submit.prevent="handleCreateProfile" class="space-y-4">
          <div>
            <label class="block text-xs font-medium text-venly-muted mb-1">Nombre completo</label>
            <input
              v-model="nombre"
              type="text"
              required
              placeholder="Ej. Ana Pérez"
              class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-accent outline-none transition-all"
            />
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-medium text-venly-muted mb-1">Cédula</label>
              <input
                v-model="cedula"
                type="text"
                required
                placeholder="12345678"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-accent outline-none transition-all"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-venly-muted mb-1">Teléfono</label>
              <input
                v-model="telefono"
                type="tel"
                required
                placeholder="04121234567"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-accent outline-none transition-all"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
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
                placeholder="Mínimo 6, letras y números"
                class="w-full px-4 py-2.5 rounded-xl border border-venly-border bg-venly-bg/80 focus:ring-2 focus:ring-venly-accent outline-none transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full mt-4 py-3 rounded-xl font-bold bg-venly-accent text-white hover:opacity-90 active:scale-[0.98] disabled:opacity-50 transition-all shadow-glow"
          >
            {{ loading ? 'Creando perfil...' : 'Crear perfil' }}
          </button>
        </form>

        <!-- Pie de tarjeta con opción de regresar -->
        <div class="text-center text-xs text-venly-muted pt-3 border-t border-venly-border/40">
          ¿Ya tienes cuenta?
          <router-link to="/login" class="text-venly-gold font-semibold hover:underline ml-1">
            Inicia sesión aquí
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>