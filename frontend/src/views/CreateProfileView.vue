<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { createProfile } from '@/services/profileService';
import { HttpError } from '@/services/httpClient';
import type { CreateProfilePayload, Role } from '@/types/profile';

const form = reactive<CreateProfilePayload>({
  nombre: '',
  cedula: '',
  correo: '',
  telefono: '',
  contrasena: '',
  rol: 'CLIENTE',
  claveOrganizacion: ''
});

const loading = ref(false);
const successMessage = ref('');
const globalError = ref('');
const fieldErrors = ref<Record<string, string>>({});

const needsOrganizationKey = computed(() => form.rol === 'GERENTE' || form.rol === 'JEFE');

watch(
  () => form.rol,
  (newRole: Role) => {
    if (newRole === 'CLIENTE') {
      form.claveOrganizacion = '';
    }
  }
);

function validateClient(): boolean {
  const errors: Record<string, string> = {};

  if (!form.nombre.trim()) errors.nombre = 'El nombre es obligatorio';
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

function clearForm() {
  form.nombre = '';
  form.cedula = '';
  form.correo = '';
  form.telefono = '';
  form.contrasena = '';
  form.rol = 'CLIENTE';
  form.claveOrganizacion = '';
}

async function submitForm() {
  successMessage.value = '';
  globalError.value = '';

  if (!validateClient()) return;

  loading.value = true;
  try {
    await createProfile({
      ...form,
      claveOrganizacion: needsOrganizationKey.value ? form.claveOrganizacion?.trim() : undefined
    });
    successMessage.value = 'Perfil creado exitosamente.';
    fieldErrors.value = {};
    clearForm();
  } catch (error) {
    if (error instanceof HttpError) {
      globalError.value = error.payload?.mensaje ?? 'No se pudo crear el perfil.';
      fieldErrors.value = error.payload?.errores ?? {};
    } else {
      globalError.value = 'Ocurrió un error inesperado. Intente de nuevo.';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="max-w-3xl mx-auto px-4 py-8">
    <section class="bg-white rounded-xl shadow p-6 space-y-5">
      <h2 class="text-2xl font-semibold">Crear perfil</h2>
      <p class="text-sm text-slate-500">Complete los datos para registrar un perfil en Venly.</p>

      <form class="grid grid-cols-1 md:grid-cols-2 gap-4" @submit.prevent="submitForm">
        <label class="space-y-1 md:col-span-2">
          <span class="text-sm font-medium">Nombre</span>
          <input v-model="form.nombre" type="text" class="w-full rounded-md border-slate-300" />
          <small class="text-red-600">{{ fieldErrors.nombre }}</small>
        </label>

        <label class="space-y-1">
          <span class="text-sm font-medium">Cédula</span>
          <input v-model="form.cedula" type="text" class="w-full rounded-md border-slate-300" />
          <small class="text-red-600">{{ fieldErrors.cedula }}</small>
        </label>

        <label class="space-y-1">
          <span class="text-sm font-medium">Teléfono</span>
          <input v-model="form.telefono" type="text" class="w-full rounded-md border-slate-300" />
          <small class="text-red-600">{{ fieldErrors.telefono }}</small>
        </label>

        <label class="space-y-1">
          <span class="text-sm font-medium">Correo</span>
          <input v-model="form.correo" type="email" class="w-full rounded-md border-slate-300" />
          <small class="text-red-600">{{ fieldErrors.correo }}</small>
        </label>

        <label class="space-y-1">
          <span class="text-sm font-medium">Contraseña</span>
          <input v-model="form.contrasena" type="password" class="w-full rounded-md border-slate-300" />
          <small class="text-red-600">{{ fieldErrors.contrasena }}</small>
        </label>

        <label class="space-y-1">
          <span class="text-sm font-medium">Rol</span>
          <select v-model="form.rol" class="w-full rounded-md border-slate-300">
            <option value="CLIENTE">Cliente</option>
            <option value="GERENTE">Gerente</option>
            <option value="JEFE">Jefe</option>
          </select>
        </label>

        <label v-if="needsOrganizationKey" class="space-y-1">
          <span class="text-sm font-medium">Clave de organización</span>
          <input v-model="form.claveOrganizacion" type="password" class="w-full rounded-md border-slate-300" />
          <small class="text-red-600">{{ fieldErrors.claveOrganizacion }}</small>
        </label>

        <div class="md:col-span-2 flex flex-col gap-3 pt-2">
          <button
            type="submit"
            :disabled="loading"
            class="inline-flex justify-center rounded-md bg-venly-primary px-4 py-2 text-white font-medium disabled:opacity-60"
          >
            {{ loading ? 'Guardando...' : 'Crear perfil' }}
          </button>

          <p v-if="successMessage" class="text-emerald-700 text-sm">{{ successMessage }}</p>
          <p v-if="globalError" class="text-red-700 text-sm">{{ globalError }}</p>
        </div>
      </form>
    </section>
  </main>
</template>
