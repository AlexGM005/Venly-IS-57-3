<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useSession } from '@/composables/useSession';
import { register } from '@/services/authService';
import { parseApiError } from '@/services/httpClient';
import type { Profile, RegisterPayload } from '@/types/profile';
import {
  compactErrors,
  validateCedula,
  validateEmail,
  validateName,
  validatePassword,
  validatePhone
} from '@/utils/validators';

type RegisterField = keyof RegisterPayload;

const router = useRouter();
const { currentUser } = useSession();

// Seguridad: Si no es JEFE, se expulsa al menú
onMounted(() => {
  if (currentUser.value?.rol !== 'JEFE') {
    router.replace({ name: 'menu' });
  }
});

// Formulario fijado para dar de alta exclusivamente GERENTES
const registerForm = reactive<RegisterPayload>({
  nombre: '',
  cedula: '',
  correo: '',
  telefono: '',
  contrasena: '',
  rol: 'GERENTE',
  claveOrganizacion: 'Venly2024' // Tu clave por defecto para roles de staff
});

const registerErrors = ref<Partial<Record<RegisterField, string>>>({});
const registerError = ref('');
const registerLoading = ref(false);
const createdManager = ref<Profile | null>(null);

function inputClass(error?: string): string[] {
  return [
    'w-full rounded-xl border border-venly-border bg-venly-bg/80 px-4 py-2.5 text-venly-text focus:ring-2 focus:ring-venly-accent',
    error ? 'venly-input-error' : ''
  ];
}

function resetForm() {
  registerForm.nombre = '';
  registerForm.cedula = '';
  registerForm.correo = '';
  registerForm.telefono = '';
  registerForm.contrasena = '';
  registerForm.claveOrganizacion = 'Venly2024';
  registerErrors.value = {};
  registerError.value = '';
  createdManager.value = null;
}

async function submitRegister() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Creación de perfil Gerente (uso del Jefe)\nDe: RegistrarGerenteView.vue -> Hacia: authService.ts");
  registerError.value = '';
  createdManager.value = null;

  registerErrors.value = compactErrors({
    nombre: validateName(registerForm.nombre),
    cedula: validateCedula(registerForm.cedula),
    correo: validateEmail(registerForm.correo),
    telefono: validatePhone(registerForm.telefono),
    contrasena: validatePassword(registerForm.contrasena),
    claveOrganizacion: !registerForm.claveOrganizacion?.trim()
      ? 'Debe indicar la clave de organización'
      : undefined
  });

  if (Object.keys(registerErrors.value).length) return;

  registerLoading.value = true;
  try {
    const profile = await register({
      ...registerForm,
      rol: 'GERENTE',
      nombre: registerForm.nombre.trim(),
      cedula: registerForm.cedula.trim(),
      correo: registerForm.correo.trim(),
      telefono: registerForm.telefono.trim(),
      claveOrganizacion: registerForm.claveOrganizacion?.trim()
    });

    // Guardamos el perfil para mostrar mensaje de éxito (NO llamamos a setSession para no desloguear al jefe)
    createdManager.value = profile;
  } catch (error) {
    const { message, fields } = parseApiError(error, 'No se pudo crear el perfil de gerente.');
    registerError.value = message;
    registerErrors.value = fields;
    if (/clave de organización/i.test(message)) {
      registerErrors.value.claveOrganizacion = message;
    }
  } finally {
    registerLoading.value = false;
  }
}
</script>

<template>
  <section class="mx-auto flex w-full max-w-xl animate-fade-up flex-col gap-6">
    <header class="space-y-2 text-center">
      <p class="text-xs font-bold uppercase tracking-widest text-venly-gold">ADMINISTRACIÓN</p>
      <h1 class="text-3xl font-extrabold tracking-tight">Registrar Nuevo Gerente 👔</h1>
      <p class="text-sm text-venly-muted">
        Crea una cuenta con permisos para gestionar salas y películas.
      </p>
    </header>

    <div>
      <!-- Mensaje de éxito al registrar -->
      <Transition name="fade">
        <div v-if="createdManager" class="venly-alert-success mb-6 rounded-2xl p-5 shadow-lg" role="status">
          <div class="flex items-center gap-3">
            <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-emerald-500 text-white">
              ✓
            </span>
            <div>
              <p class="font-bold text-white">¡Gerente registrado con éxito!</p>
              <p class="text-xs text-venly-muted">
                Se creó la cuenta para <strong class="text-venly-text">{{ createdManager.nombre }}</strong> ({{ createdManager.correo }}).
              </p>
            </div>
          </div>
          <div class="mt-4 flex gap-3">
            <button
              type="button"
              class="rounded-xl bg-venly-bg/80 px-4 py-2 text-xs font-bold text-venly-text hover:bg-venly-bg"
              @click="resetForm"
            >
              + Registrar otro gerente
            </button>
            <RouterLink
              to="/menu"
              class="rounded-xl bg-venly-accent/20 px-4 py-2 text-xs font-bold text-venly-accent hover:bg-venly-accent/30"
            >
              Volver al menú
            </RouterLink>
          </div>
        </div>
      </Transition>

      <!-- Formulario -->
      <div class="venly-card space-y-5 rounded-2xl border border-venly-border bg-venly-card/40 p-6 shadow-xl sm:p-8">
        <form class="grid grid-cols-1 gap-x-5 gap-y-3 sm:grid-cols-2" novalidate @submit.prevent="submitRegister">
          <div v-if="registerError" class="venly-alert-error sm:col-span-2" role="alert">
            {{ registerError }}
          </div>

          <div class="sm:col-span-2">
            <label for="nombre" class="venly-label">Nombre completo</label>
            <input
              id="nombre"
              v-model="registerForm.nombre"
              type="text"
              autocomplete="name"
              placeholder="Ej. Roberto Morales"
              :class="inputClass(registerErrors.nombre)"
            />
            <p class="venly-field-error">{{ registerErrors.nombre }}</p>
          </div>

          <div>
            <label for="cedula" class="venly-label">Cédula</label>
            <input
              id="cedula"
              v-model="registerForm.cedula"
              type="text"
              inputmode="numeric"
              placeholder="12345678"
              :class="inputClass(registerErrors.cedula)"
            />
            <p class="venly-field-error">{{ registerErrors.cedula }}</p>
          </div>

          <div>
            <label for="telefono" class="venly-label">Teléfono</label>
            <input
              id="telefono"
              v-model="registerForm.telefono"
              type="tel"
              inputmode="numeric"
              placeholder="04121234567"
              :class="inputClass(registerErrors.telefono)"
            />
            <p class="venly-field-error">{{ registerErrors.telefono }}</p>
          </div>

          <div class="sm:col-span-2">
            <label for="correo" class="venly-label">Correo electrónico</label>
            <input
              id="correo"
              v-model="registerForm.correo"
              type="email"
              autocomplete="email"
              placeholder="gerente@cine.com"
              :class="inputClass(registerErrors.correo)"
            />
            <p class="venly-field-error">{{ registerErrors.correo }}</p>
          </div>

          <div class="sm:col-span-2">
            <label for="contrasena" class="venly-label">Contraseña provisional</label>
            <input
              id="contrasena"
              v-model="registerForm.contrasena"
              type="password"
              autocomplete="new-password"
              placeholder="Mínimo 6 caracteres"
              :class="inputClass(registerErrors.contrasena)"
            />
            <p class="venly-field-error">{{ registerErrors.contrasena }}</p>
          </div>

          <div class="sm:col-span-2">
            <label for="claveOrganizacion" class="venly-label">
              Clave de organización <span class="text-venly-gold">· Requerida para Gerente</span>
            </label>
            <input
              id="claveOrganizacion"
              v-model="registerForm.claveOrganizacion"
              type="password"
              placeholder="Venly2024"
              :class="inputClass(registerErrors.claveOrganizacion)"
            />
            <p class="venly-field-error">{{ registerErrors.claveOrganizacion }}</p>
          </div>

          <div class="mt-4 flex flex-col gap-3 sm:col-span-2">
            <button
              type="submit"
              class="venly-btn-primary w-full"
              :disabled="registerLoading"
            >
              <LoadingSpinner v-if="registerLoading" />
              {{ registerLoading ? 'Registrando gerente…' : 'Registrar Gerente' }}
            </button>

            <RouterLink
              to="/menu"
              class="pt-1 text-center text-xs font-semibold text-venly-muted transition-colors hover:text-venly-text"
            >
              ← Cancelar y volver al menú
            </RouterLink>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>