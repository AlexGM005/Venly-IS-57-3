<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { createProfile } from '@/services/profileService';
import { HttpError } from '@/services/httpClient';
import { useRouter } from 'vue-router';
import { useSession } from '@/composables/useSession';
import type { CreateProfilePayload, Profile, Role } from '@/types/profile';

type FormField = keyof CreateProfilePayload;

const form = reactive<CreateProfilePayload>({
  nombre: '',
  cedula: '',
  correo: '',
  telefono: '',
  contrasena: '',
  rol: 'CLIENTE',
  claveOrganizacion: ''
});

const roleOptions: { value: Role; label: string; description: string }[] = [
  { value: 'CLIENTE', label: 'Cliente', description: 'Compra boletos y reserva funciones' },
  { value: 'GERENTE', label: 'Gerente', description: 'Gestiona cartelera y salas' },
  { value: 'JEFE', label: 'Jefe', description: 'Administra toda la organización' }
];

const loading = ref(false);
const router = useRouter();
const { login } = useSession();
const REDIRECT_DELAY_MS = 1200;

const createdProfile = ref<Profile | null>(null);
const globalError = ref('');
const fieldErrors = ref<Partial<Record<FormField, string>>>({});

const needsOrganizationKey = computed(() => form.rol === 'GERENTE' || form.rol === 'JEFE');

watch(
  () => form.rol,
  (role) => {
    if (role === 'CLIENTE') {
      form.claveOrganizacion = '';
      delete fieldErrors.value.claveOrganizacion;
    }
  }
);

function inputClass(field: FormField): string[] {
  return ['venly-input', fieldErrors.value[field] ? 'venly-input-error' : ''];
}

function validateClient(): boolean {
  const errors: Partial<Record<FormField, string>> = {};

  if (!form.nombre.trim()) {
    errors.nombre = 'El nombre es obligatorio';
  } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]+$/.test(form.nombre.trim())) {
    errors.nombre = 'El nombre solo puede contener letras y espacios';
  }
  if (!/^[0-9]{6,12}$/.test(form.cedula.trim())) errors.cedula = 'La cédula debe contener entre 6 y 12 dígitos';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correo.trim())) errors.correo = 'El correo no tiene un formato válido';
  if (!/^[0-9]{10,15}$/.test(form.telefono.trim())) errors.telefono = 'El teléfono debe contener entre 10 y 15 dígitos';
  if (form.contrasena.length < 6) {
    errors.contrasena = 'La contraseña debe tener al menos 6 caracteres';
  } else if (!/^(?=.*[A-Za-z])(?=.*[0-9]).+$/.test(form.contrasena)) {
    errors.contrasena = 'La contraseña debe contener letras y números';
  }
  if (needsOrganizationKey.value && !form.claveOrganizacion?.trim()) {
    errors.claveOrganizacion = 'Debe indicar la clave de organización para este rol';
  }

  fieldErrors.value = errors;
  return Object.keys(errors).length === 0;
}

function resetForm() {
  Object.assign(form, {
    nombre: '',
    cedula: '',
    correo: '',
    telefono: '',
    contrasena: '',
    rol: 'CLIENTE',
    claveOrganizacion: ''
  });
}

async function submitForm() {
  createdProfile.value = null;
  globalError.value = '';
  if (!validateClient()) return;

  loading.value = true;
  try {
    const profile = await createProfile({
      ...form,
      claveOrganizacion: needsOrganizationKey.value ? form.claveOrganizacion?.trim() : undefined
    });
    login(profile);
    createdProfile.value = profile;
    fieldErrors.value = {};
    resetForm();
    // Short pause so the success animation is visible before entering the main menu.
    setTimeout(() => router.push({ name: 'menu' }), REDIRECT_DELAY_MS);
  } catch (error) {
    if (error instanceof HttpError) {
      globalError.value = error.payload?.mensaje ?? 'No se pudo crear el perfil.';
      fieldErrors.value = (error.payload?.errores ?? {}) as Partial<Record<FormField, string>>;
    } else {
      globalError.value = 'No se pudo conectar con el servidor. Verifique que el backend esté activo.';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="mx-auto max-w-3xl animate-fade-up">
    <header class="mb-8 text-center">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Registro</p>
      <h1 class="text-3xl font-extrabold sm:text-4xl">Crea tu perfil en Venly</h1>
      <p class="mt-2 text-venly-muted">Únete y vive la mejor experiencia de cine.</p>
    </header>

    <Transition name="fade">
      <div
        v-if="createdProfile"
        class="mb-6 flex flex-col items-start gap-4 rounded-xl border border-emerald-500/40 bg-emerald-500/10 p-4 sm:flex-row sm:items-center"
        role="status"
      >
        <span class="flex h-11 w-11 shrink-0 animate-pop items-center justify-center rounded-full bg-emerald-500 text-white shadow-lg shadow-emerald-500/30">
          <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
            <path class="animate-draw" stroke-dasharray="24" stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        </span>
        <div class="flex-1">
          <p class="font-semibold text-emerald-300">¡Perfil creado exitosamente!</p>
          <p class="text-sm text-venly-muted">
            Bienvenido, {{ createdProfile.nombre }}. Tu ID de perfil es <strong class="text-venly-text">#{{ createdProfile.id }}</strong>. Entrando a tu menú…
          </p>
        </div>
        <LoadingSpinner class="text-emerald-300" />
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="globalError" class="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-sm text-red-300" role="alert">
        {{ globalError }}
      </div>
    </Transition>

    <form class="venly-card grid grid-cols-1 gap-x-5 gap-y-2 p-6 sm:p-8 md:grid-cols-2" novalidate @submit.prevent="submitForm">
      <div class="md:col-span-2">
        <label for="nombre" class="venly-label">Nombre completo</label>
        <input id="nombre" v-model="form.nombre" type="text" autocomplete="name" placeholder="Ej. Ana Pérez" :class="inputClass('nombre')" />
        <p class="venly-field-error">{{ fieldErrors.nombre }}</p>
      </div>

      <div>
        <label for="cedula" class="venly-label">Cédula</label>
        <input id="cedula" v-model="form.cedula" type="text" inputmode="numeric" placeholder="12345678" :class="inputClass('cedula')" />
        <p class="venly-field-error">{{ fieldErrors.cedula }}</p>
      </div>

      <div>
        <label for="telefono" class="venly-label">Teléfono</label>
        <input id="telefono" v-model="form.telefono" type="tel" inputmode="numeric" placeholder="04121234567" :class="inputClass('telefono')" />
        <p class="venly-field-error">{{ fieldErrors.telefono }}</p>
      </div>

      <div>
        <label for="correo" class="venly-label">Correo electrónico</label>
        <input id="correo" v-model="form.correo" type="email" autocomplete="email" placeholder="tu@correo.com" :class="inputClass('correo')" />
        <p class="venly-field-error">{{ fieldErrors.correo }}</p>
      </div>

      <div>
        <label for="contrasena" class="venly-label">Contraseña</label>
        <input id="contrasena" v-model="form.contrasena" type="password" autocomplete="new-password" placeholder="Mínimo 6, letras y números" :class="inputClass('contrasena')" />
        <p class="venly-field-error">{{ fieldErrors.contrasena }}</p>
      </div>

      <fieldset class="md:col-span-2">
        <legend class="venly-label">Rol</legend>
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <label
            v-for="option in roleOptions"
            :key="option.value"
            class="cursor-pointer rounded-xl border p-3.5 transition-all duration-200 hover:border-venly-accent/60"
            :class="form.rol === option.value
              ? 'border-venly-accent bg-venly-accent/10 shadow-glow'
              : 'border-venly-border bg-venly-bg/50'"
          >
            <input v-model="form.rol" type="radio" name="rol" :value="option.value" class="sr-only" />
            <span class="block font-semibold" :class="form.rol === option.value ? 'text-venly-text' : 'text-gray-300'">
              {{ option.label }}
            </span>
            <span class="mt-0.5 block text-xs text-venly-muted">{{ option.description }}</span>
          </label>
        </div>
      </fieldset>

      <Transition name="expand">
        <div v-if="needsOrganizationKey" class="mt-3 md:col-span-2">
          <label for="claveOrganizacion" class="venly-label">
            Clave de organización <span class="text-venly-gold">· requerida para {{ form.rol === 'JEFE' ? 'Jefe' : 'Gerente' }}</span>
          </label>
          <input
            id="claveOrganizacion"
            v-model="form.claveOrganizacion"
            type="password"
            placeholder="Clave proporcionada por la organización"
            :class="inputClass('claveOrganizacion')"
          />
          <p class="venly-field-error">{{ fieldErrors.claveOrganizacion }}</p>
          <p class="text-xs text-venly-gold/90">🔑 Clave de prueba: <code class="font-semibold">Venly2024</code></p>
        </div>
      </Transition>

      <div class="mt-4 md:col-span-2">
        <button type="submit" :disabled="loading || createdProfile !== null" class="venly-btn-primary w-full sm:w-auto">
          <LoadingSpinner v-if="loading" />
          {{ loading ? 'Creando perfil...' : 'Crear perfil' }}
        </button>
      </div>
    </form>
  </section>
</template>
