<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import BaseModal from '@/components/BaseModal.vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import ProfileCard from '@/components/ProfileCard.vue';
import { useSession } from '@/composables/useSession';
import { parseApiError } from '@/services/httpClient';
import { getMyProfile, updateMyProfile } from '@/services/profileService';
import type { UpdateProfilePayload } from '@/types/profile';
import { compactErrors, validateEmail, validateName, validatePhone } from '@/utils/validators';

type Field = keyof UpdateProfilePayload;

const { currentUser, setSession } = useSession();

const loadError = ref('');
const editOpen = ref(false);
const saving = ref(false);
const saveError = ref('');
const successMessage = ref('');
const form = reactive<UpdateProfilePayload>({ nombre: '', correo: '', telefono: '' });
const errors = ref<Partial<Record<Field, string>>>({});

// Refresh the stored session with the latest data from the server.
onMounted(async () => {
  try {
    setSession(await getMyProfile());
  } catch (error) {
    loadError.value = parseApiError(error, 'No se pudo actualizar la información del perfil.').message;
  }
});

function openEdit() {
  if (!currentUser.value) return;
  Object.assign(form, {
    nombre: currentUser.value.nombre,
    correo: currentUser.value.correo,
    telefono: currentUser.value.telefono
  });
  errors.value = {};
  saveError.value = '';
  editOpen.value = true;
}

async function save() {
  saveError.value = '';
  errors.value = compactErrors({
    nombre: validateName(form.nombre),
    correo: validateEmail(form.correo),
    telefono: validatePhone(form.telefono)
  });
  if (Object.keys(errors.value).length) return;

  saving.value = true;
  try {
    const updated = await updateMyProfile({
      nombre: form.nombre.trim(),
      correo: form.correo.trim(),
      telefono: form.telefono.trim()
    });
    setSession(updated);
    editOpen.value = false;
    successMessage.value = 'Tu perfil se actualizó correctamente.';
    setTimeout(() => (successMessage.value = ''), 4000);
  } catch (error) {
    const parsed = parseApiError(error, 'No se pudo guardar el perfil.');
    saveError.value = parsed.message;
    errors.value = parsed.fields;
    if (/correo/i.test(parsed.message) && !parsed.fields.correo) errors.value.correo = parsed.message;
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <section v-if="currentUser" class="mx-auto max-w-3xl animate-fade-up">
    <RouterLink to="/menu" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver al menú</RouterLink>
    <header class="mb-6">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Perfil</p>
      <h1 class="text-3xl font-extrabold">Mi perfil</h1>
    </header>

    <Transition name="fade">
      <div v-if="successMessage" class="venly-alert-success mb-6" role="status">✅ {{ successMessage }}</div>
    </Transition>
    <div v-if="loadError" class="venly-alert-error mb-6" role="alert">{{ loadError }}</div>

    <ProfileCard :profile="currentUser">
      <template #actions>
        <!-- <button type="button" class="venly-btn-primary" @click="openEdit">✏️ Editar perfil</button> -->
      </template>
    </ProfileCard>

    <BaseModal :open="editOpen" title="Editar perfil" @close="editOpen = false">
      <form class="space-y-1" novalidate @submit.prevent="save">
        <p class="mb-4 text-sm text-venly-muted">La cédula y el rol no se pueden modificar.</p>
        <div v-if="saveError" class="venly-alert-error mb-3" role="alert">{{ saveError }}</div>
        <div>
          <label for="edit-nombre" class="venly-label">Nombre completo</label>
          <input id="edit-nombre" v-model="form.nombre" type="text" autocomplete="name" class="venly-input" :class="{ 'venly-input-error': errors.nombre }" />
          <p class="venly-field-error">{{ errors.nombre }}</p>
        </div>
        <div>
          <label for="edit-correo" class="venly-label">Correo electrónico</label>
          <input id="edit-correo" v-model="form.correo" type="email" autocomplete="email" class="venly-input" :class="{ 'venly-input-error': errors.correo }" />
          <p class="venly-field-error">{{ errors.correo }}</p>
        </div>
        <div>
          <label for="edit-telefono" class="venly-label">Teléfono</label>
          <input id="edit-telefono" v-model="form.telefono" type="tel" inputmode="numeric" class="venly-input" :class="{ 'venly-input-error': errors.telefono }" />
          <p class="venly-field-error">{{ errors.telefono }}</p>
        </div>
        <div class="grid grid-cols-2 gap-3 pt-2">
          <div class="rounded-lg border border-venly-border bg-venly-bg/40 p-3 text-sm">
            <span class="block text-xs text-venly-muted">Cédula</span>{{ currentUser.cedula }}
          </div>
          <div class="rounded-lg border border-venly-border bg-venly-bg/40 p-3 text-sm">
            <span class="block text-xs text-venly-muted">Rol</span>{{ currentUser.rol }}
          </div>
        </div>
        <div class="flex justify-end gap-3 pt-5">
          <button type="button" class="venly-btn-secondary" :disabled="saving" @click="editOpen = false">Cancelar</button>
          <button type="submit" class="venly-btn-primary" :disabled="saving">
            <LoadingSpinner v-if="saving" />
            {{ saving ? 'Guardando…' : 'Guardar cambios' }}
          </button>
        </div>
      </form>
    </BaseModal>
  </section>
</template>
