<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useSession } from '@/composables/useSession';
import { login, register } from '@/services/authService';
import { parseApiError } from '@/services/httpClient';
import type { LoginPayload, Profile, RegisterPayload, Role } from '@/types/profile';
import {
  compactErrors,
  validateCedula,
  validateEmail,
  validateName,
  validatePassword,
  validatePhone
} from '@/utils/validators';

type Tab = 'login' | 'registro';
type RegisterField = keyof RegisterPayload;
type LoginField = keyof LoginPayload;

const route = useRoute();
const router = useRouter();
const { setSession } = useSession();
const REDIRECT_DELAY_MS = 900;

const activeTab = ref<Tab>(route.query.tab === 'registro' ? 'registro' : 'login');

// ---- Login ----
const loginForm = reactive<LoginPayload>({ correo: '', contrasena: '' });
const loginErrors = ref<Partial<Record<LoginField, string>>>({});
const loginError = ref('');
const loginLoading = ref(false);

// ---- Registro ----
const registerForm = reactive<RegisterPayload>({
  nombre: '',
  cedula: '',
  correo: '',
  telefono: '',
  contrasena: '',
  rol: 'CLIENTE',
  claveOrganizacion: ''
});
const registerErrors = ref<Partial<Record<RegisterField, string>>>({});
const registerError = ref('');
const registerLoading = ref(false);

const welcomeProfile = ref<Profile | null>(null);

const roleOptions: { value: Role; label: string; description: string; icon: string }[] = [
  // { value: 'CLIENTE', label: 'Cliente', description: 'Disfruta la cartelera', icon: '🍿' }  
];

const needsOrganizationKey = computed(() => registerForm.rol === 'GERENTE' || registerForm.rol === 'JEFE');

watch(
  () => registerForm.rol,
  (role) => {
    if (role === 'CLIENTE') {
      registerForm.claveOrganizacion = '';
      delete registerErrors.value.claveOrganizacion;
    }
  }
);

function switchTab(tab: Tab) {
  activeTab.value = tab;
  loginError.value = '';
  registerError.value = '';
  router.replace({ query: tab === 'registro' ? { tab } : {} });
}

function inputClass(error?: string): string[] {
  return [
    'w-full rounded-xl border border-venly-border bg-venly-bg/80 px-4 py-2.5 text-venly-text focus:ring-2 focus:ring-venly-accent',
    error ? 'venly-input-error' : ''
  ];
}

/** Stores the profile as the simulated session and opens the main menu after a short confirmation. */
function enterApp(profile: Profile) {
  setSession(profile);
  welcomeProfile.value = profile;
  setTimeout(() => router.push({ name: 'menu' }), REDIRECT_DELAY_MS);
}

async function submitLogin() {
  loginError.value = '';
  loginErrors.value = compactErrors({
    correo: validateEmail(loginForm.correo),
    contrasena: loginForm.contrasena ? undefined : 'La contraseña es obligatoria'
  });
  if (Object.keys(loginErrors.value).length) return;

  loginLoading.value = true;
  try {
    enterApp(await login({ correo: loginForm.correo.trim(), contrasena: loginForm.contrasena }));
  } catch (error) {
    const { message, fields } = parseApiError(error, 'No se pudo iniciar sesión.');
    loginError.value = message;
    loginErrors.value = fields;
  } finally {
    loginLoading.value = false;
  }
}

async function submitRegister() {
  registerError.value = '';
  registerErrors.value = compactErrors({
    nombre: validateName(registerForm.nombre),
    cedula: validateCedula(registerForm.cedula),
    correo: validateEmail(registerForm.correo),
    telefono: validatePhone(registerForm.telefono),
    contrasena: validatePassword(registerForm.contrasena),
    claveOrganizacion:
      needsOrganizationKey.value && !registerForm.claveOrganizacion?.trim()
        ? 'Debe indicar la clave de organización para este rol'
        : undefined
  });
  if (Object.keys(registerErrors.value).length) return;

  registerLoading.value = true;
  try {
    const profile = await register({
      ...registerForm,
      nombre: registerForm.nombre.trim(),
      cedula: registerForm.cedula.trim(),
      correo: registerForm.correo.trim(),
      telefono: registerForm.telefono.trim(),
      claveOrganizacion: needsOrganizationKey.value ? registerForm.claveOrganizacion?.trim() : undefined
    });
    enterApp(profile);
  } catch (error) {
    const { message, fields } = parseApiError(error, 'No se pudo crear el perfil.');
    registerError.value = message;
    registerErrors.value = fields;
    if (/clave de organización/i.test(message)) registerErrors.value.claveOrganizacion = message;
  } finally {
    registerLoading.value = false;
  }
}
</script>

<template>
  <section class="mx-auto flex w-full max-w-md animate-fade-up flex-col gap-6">
    <header class="space-y-2 text-center">
      <p class="text-xs font-bold uppercase tracking-widest text-venly-gold">
        {{ activeTab === 'login' ? 'BIENVENIDO DE NUEVO' : 'REGISTRO' }}
      </p>
      <h1 class="text-3xl font-extrabold tracking-tight">
        {{ activeTab === 'login' ? 'Iniciaaaa sesión en Venly' : 'Crea tu perfil en Venly' }}
      </h1>
      <p class="text-sm text-venly-muted">
        {{ activeTab === 'login' ? 'Ingresa tus datos para acceder a tu cuenta.' : 'Completa tus datos para crear tu cuenta.' }}
      </p>
    </header>

    <div>
      <Transition name="fade">
        <div v-if="welcomeProfile" class="venly-alert-success mb-6 flex items-center gap-4" role="status">
          <span class="flex h-11 w-11 shrink-0 animate-pop items-center justify-center rounded-full bg-emerald-500 text-white shadow-lg shadow-emerald-500/30">
            <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
              <path class="animate-draw" stroke-dasharray="24" stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </span>
          <div class="flex-1">
            <p class="font-semibold">¡Bienvenido, {{ welcomeProfile.nombre }}!</p>
            <p class="text-venly-muted">Entrando a tu menú principal…</p>
          </div>
          <LoadingSpinner />
        </div>
      </Transition>

      <div class="venly-card space-y-5 rounded-2xl border border-venly-border bg-venly-card/40 p-6 shadow-xl sm:p-8">
        <Transition name="tab" mode="out-in">
          <form v-if="activeTab === 'login'" key="login" class="space-y-4" novalidate @submit.prevent="submitLogin">
            <div v-if="loginError" class="venly-alert-error" role="alert">{{ loginError }}</div>
            <div>
              <label for="login-correo" class="mb-1 block text-xs font-medium text-venly-muted">Correo electrónico</label>
              <input id="login-correo" v-model="loginForm.correo" type="email" autocomplete="email" placeholder="tu@correo.com" :class="inputClass(loginErrors.correo)" />
              <p v-if="loginErrors.correo" class="venly-field-error">{{ loginErrors.correo }}</p>
            </div>
            <div>
              <label for="login-contrasena" class="mb-1 block text-xs font-medium text-venly-muted">Contraseña</label>
              <input id="login-contrasena" v-model="loginForm.contrasena" type="password" autocomplete="current-password" placeholder="••••••••" :class="inputClass(loginErrors.contrasena)" />
              <p v-if="loginErrors.contrasena" class="venly-field-error">{{ loginErrors.contrasena }}</p>
            </div>
            <button
              type="submit"
              class="w-full rounded-xl bg-venly-accent py-3 font-bold text-white shadow-glow transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="loginLoading || welcomeProfile !== null"
            >
              <LoadingSpinner v-if="loginLoading" />
              {{ loginLoading ? 'Iniciando...' : 'Iniciar sesión' }}
            </button>
          </form>

          <form v-else key="registro" class="grid grid-cols-1 gap-x-5 gap-y-1 sm:grid-cols-2" novalidate @submit.prevent="submitRegister">
            <div v-if="registerError" class="venly-alert-error mb-2 sm:col-span-2" role="alert">{{ registerError }}</div>

            <div class="sm:col-span-2">
              <label for="nombre" class="venly-label">Nombre completo</label>
              <input id="nombre" v-model="registerForm.nombre" type="text" autocomplete="name" placeholder="Ej. Ana Pérez" :class="inputClass(registerErrors.nombre)" />
              <p class="venly-field-error">{{ registerErrors.nombre }}</p>
            </div>
            <div>
              <label for="cedula" class="venly-label">Cédula</label>
              <input id="cedula" v-model="registerForm.cedula" type="text" inputmode="numeric" placeholder="12345678" :class="inputClass(registerErrors.cedula)" />
              <p class="venly-field-error">{{ registerErrors.cedula }}</p>
            </div>
            <div>
              <label for="telefono" class="venly-label">Teléfono</label>
              <input id="telefono" v-model="registerForm.telefono" type="tel" inputmode="numeric" placeholder="04121234567" :class="inputClass(registerErrors.telefono)" />
              <p class="venly-field-error">{{ registerErrors.telefono }}</p>
            </div>
            <div>
              <label for="correo" class="venly-label">Correo electrónico</label>
              <input id="correo" v-model="registerForm.correo" type="email" autocomplete="email" placeholder="tu@correo.com" :class="inputClass(registerErrors.correo)" />
              <p class="venly-field-error">{{ registerErrors.correo }}</p>
            </div>
            <div>
              <label for="contrasena" class="venly-label">Contraseña</label>
              <input id="contrasena" v-model="registerForm.contrasena" type="password" autocomplete="new-password" placeholder="Mínimo 6, letras y números" :class="inputClass(registerErrors.contrasena)" />
              <p class="venly-field-error">{{ registerErrors.contrasena }}</p>
            </div>

            <fieldset class="sm:col-span-2">
              <div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
                <label
                  v-for="option in roleOptions"
                  :key="option.value"
                  class="cursor-pointer rounded-xl border p-3 transition-all duration-200 hover:border-venly-accent/60 focus-within:ring-2 focus-within:ring-venly-accent/50"
                  :class="registerForm.rol === option.value ? 'border-venly-accent bg-venly-accent/10 shadow-glow' : 'border-venly-border bg-venly-bg/50'"
                >
                  <input v-model="registerForm.rol" type="radio" name="rol" :value="option.value" class="sr-only" />
                  <span class="flex items-center gap-2 font-semibold">
                    <span aria-hidden="true">{{ option.icon }}</span>{{ option.label }}
                  </span>
                  <span class="mt-0.5 block text-xs text-venly-muted">{{ option.description }}</span>
                </label>
              </div>
            </fieldset>

            <Transition name="expand">
              <div v-if="needsOrganizationKey" class="mt-3 sm:col-span-2">
                <label for="claveOrganizacion" class="venly-label">
                  Clave de organización <span class="text-venly-gold">· requerida para {{ registerForm.rol === 'JEFE' ? 'Jefe' : 'Gerente' }}</span>
                </label>
                <input
                  id="claveOrganizacion"
                  v-model="registerForm.claveOrganizacion"
                  type="password"
                  autocomplete="off"
                  placeholder="Clave proporcionada por la organización"
                  :class="inputClass(registerErrors.claveOrganizacion)"
                />
                <p class="venly-field-error">{{ registerErrors.claveOrganizacion }}</p>
                <p class="text-xs text-venly-gold/90">🔑 Clave de prueba: <code class="font-semibold">Venly2024</code></p>
              </div>
            </Transition>

            <div class="mt-4 sm:col-span-2">
              <button type="submit" class="venly-btn-primary w-full" :disabled="registerLoading || welcomeProfile !== null">
                <LoadingSpinner v-if="registerLoading" />
                {{ registerLoading ? 'Creando perfil…' : 'Crear perfil' }}
              </button>
              <p class="pt-3 text-center text-sm text-venly-muted">
                ¿Ya tienes cuenta?
                <button type="button" class="font-semibold text-venly-accent hover:underline" @click="switchTab('login')">Inicia sesión</button>
              </p>
            </div>
          </form>
        </Transition>

        <div v-if="activeTab === 'login'" class="border-t border-venly-border/40 pt-3 text-center text-xs text-venly-muted">
          ¿No tienes cuenta aún?
          <button type="button" class="ml-1 font-semibold text-venly-gold hover:underline" @click="switchTab('registro')">
            Crea tu perfil aquí
          </button>
        </div>
      </div>
    </div>

    <!-- <div class="fixed bottom-6 right-6 z-40">
      <RouterLink
        to="/admin-login"
        class="group flex h-12 w-12 items-center justify-center rounded-full border border-venly-gold/40 bg-venly-card text-venly-gold shadow-2xl transition-all hover:scale-110 hover:border-venly-gold hover:bg-venly-gold/10"
        title="Acceso exclusivo para Gerentes"
        aria-label="Acceso exclusivo para Gerentes"
      >
        <span class="text-xl transition-transform group-hover:rotate-12" aria-hidden="true">🛡️</span>
      </RouterLink>
    </div> -->
  </section>
</template>
