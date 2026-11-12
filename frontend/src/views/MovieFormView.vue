<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { parseApiError } from '@/services/httpClient';
import {
  ALLOWED_IMAGE_TYPES,
  MAX_IMAGE_BYTES,
  createMovie,
  getMovie,
  getRooms,
  movieImageSrc,
  updateMovie
} from '@/services/movieService';
import type { Clasificacion, ImageType, Movie, MoviePayload, Room } from '@/types/movie';

const props = defineProps<{ id?: number }>();
const isEdit = computed(() => props.id !== undefined);

type Field = 'titulo' | 'horario' | 'duracion' | 'sinopsis' | 'clasificacion' | 'salaId' | 'imagen';

interface FormState {
  titulo: string;
  horaInicio: string;
  horaFin: string;
  horas: number | null;
  minutos: number | null;
  sinopsis: string;
  clasificacion: Clasificacion | '';
  salaId: number | null;
}

const emptyForm = (): FormState => ({
  titulo: '',
  horaInicio: '',
  horaFin: '',
  horas: null,
  minutos: null,
  sinopsis: '',
  clasificacion: '',
  salaId: null
});

const clasificaciones: { value: Clasificacion; label: string }[] = [
  { value: 'G', label: 'G · Todo público' },
  { value: 'PG', label: 'PG · Guía parental sugerida' },
  { value: 'PG-13', label: 'PG-13 · Mayores de 13 años' },
  { value: 'R', label: 'R · Restringida (menores acompañados)' },
  { value: 'NC-17', label: 'NC-17 · Solo adultos' }
];
const SINOPSIS_MAX = 2000;

const form = reactive<FormState>(emptyForm());
const rooms = ref<Room[]>([]);
const errors = ref<Partial<Record<Field, string>>>({});
const globalError = ref('');
const successMessage = ref('');
const loading = ref(true);
const saving = ref(false);

// Image state: new upload (base64 without prefix) or the current image of the movie being edited.
const imageBase64 = ref<string | null>(null);
const imageType = ref<ImageType | null>(null);
const imageName = ref('');
const existingImageSrc = ref<string | null>(null);
const removeExistingImage = ref(false);
const dragOver = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const durationTouched = ref(false);

const previewSrc = computed(() => {
  if (imageBase64.value && imageType.value) return `data:${imageType.value};base64,${imageBase64.value}`;
  return removeExistingImage.value ? null : existingImageSrc.value;
});

/** Suggests the duration from the schedule until the user edits it manually. */
watch(
  () => [form.horaInicio, form.horaFin],
  () => {
    if (durationTouched.value || !form.horaInicio || !form.horaFin) return;
    const toMinutes = (t: string) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5));
    let diff = toMinutes(form.horaFin) - toMinutes(form.horaInicio);
    if (diff <= 0) diff += 24 * 60;
    form.horas = Math.floor(diff / 60);
    form.minutos = diff % 60;
  }
);

function buildDuration(): string {
  const h = form.horas ?? 0;
  const m = form.minutos ?? 0;
  if (h > 0 && m > 0) return `${h}h ${m}min`;
  if (h > 0) return `${h}h`;
  return `${m}min`;
}

function parseDuration(value: string) {
  const match = /^(?:(\d+)h)?\s*(?:(\d+)min)?$/.exec(value.trim());
  form.horas = match?.[1] ? Number(match[1]) : 0;
  form.minutos = match?.[2] ? Number(match[2]) : 0;
}

function fillForm(movie: Movie) {
  const [inicio = '', fin = ''] = movie.horario.split('-').map((t) => t.trim());
  Object.assign(form, {
    titulo: movie.titulo,
    horaInicio: inicio,
    horaFin: fin,
    sinopsis: movie.sinopsis,
    clasificacion: movie.clasificacion,
    salaId: movie.salaId
  });
  parseDuration(movie.duracion);
  durationTouched.value = true;
  existingImageSrc.value = movieImageSrc(movie);
  removeExistingImage.value = false;
  clearNewImage();
}

function clearNewImage() {
  imageBase64.value = null;
  imageType.value = null;
  imageName.value = '';
  if (fileInput.value) fileInput.value.value = '';
}

function resetForm() {
  Object.assign(form, emptyForm());
  durationTouched.value = false;
  existingImageSrc.value = null;
  removeExistingImage.value = false;
  clearNewImage();
  errors.value = {};
}

function formatSize(bytes: number) {
  return bytes >= 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.ceil(bytes / 1024)} KB`;
}

function handleFile(file: File | undefined) {
  delete errors.value.imagen;
  if (!file) return;
  if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
    errors.value.imagen = 'La imagen debe ser JPG o PNG';
    return;
  }
  if (file.size > MAX_IMAGE_BYTES) {
    errors.value.imagen = `La imagen pesa ${formatSize(file.size)}; el máximo permitido es 10 MB`;
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    const dataUrl = String(reader.result);
    imageBase64.value = dataUrl.slice(dataUrl.indexOf(',') + 1);
    imageType.value = file.type as ImageType;
    imageName.value = `${file.name} · ${formatSize(file.size)}`;
  };
  reader.onerror = () => (errors.value.imagen = 'No se pudo leer la imagen seleccionada');
  reader.readAsDataURL(file);
}

function onFileChange(event: Event) {
  handleFile((event.target as HTMLInputElement).files?.[0]);
}

function onDrop(event: DragEvent) {
  dragOver.value = false;
  handleFile(event.dataTransfer?.files?.[0]);
}

function removeImage() {
  clearNewImage();
  if (existingImageSrc.value) removeExistingImage.value = true;
}

function validate(): boolean {
  const e: Partial<Record<Field, string>> = {};
  if (!form.titulo.trim()) e.titulo = 'El título es obligatorio';
  else if (form.titulo.trim().length > 150) e.titulo = 'El título no puede exceder 150 caracteres';
  if (!form.horaInicio || !form.horaFin) e.horario = 'Indique la hora de inicio y de fin';
  else if (form.horaInicio === form.horaFin) e.horario = 'La hora de inicio y fin no pueden ser iguales';
  const h = form.horas ?? 0;
  const m = form.minutos ?? 0;
  if (h < 0 || h > 99 || m < 0 || m > 59 || !Number.isInteger(h) || !Number.isInteger(m)) e.duracion = 'Duración inválida (minutos entre 0 y 59)';
  else if (h === 0 && m === 0) e.duracion = 'La duración es obligatoria';
  if (!form.clasificacion) e.clasificacion = 'La clasificación es obligatoria';
  if (!form.salaId) e.salaId = 'La sala es obligatoria';
  if (!form.sinopsis.trim()) e.sinopsis = 'La sinopsis es obligatoria';
  else if (form.sinopsis.trim().length > SINOPSIS_MAX) e.sinopsis = `La sinopsis no puede exceder ${SINOPSIS_MAX} caracteres`;
  if (errors.value.imagen) e.imagen = errors.value.imagen;
  errors.value = e;
  return Object.keys(e).length === 0;
}

async function submit() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Envío de formulario de película (Gerente/Jefe)\nDe: MovieFormView.vue -> Hacia: movieService.ts");
  globalError.value = '';
  successMessage.value = '';
  if (!validate()) return;

  const payload: MoviePayload = {
    titulo: form.titulo.trim(),
    horario: `${form.horaInicio} - ${form.horaFin}`,
    duracion: buildDuration(),
    sinopsis: form.sinopsis.trim(),
    clasificacion: form.clasificacion,
    salaId: form.salaId,
    imagenBase64: imageBase64.value,
    imagenTipo: imageType.value,
    quitarImagen: isEdit.value && removeExistingImage.value && !imageBase64.value
  };

  saving.value = true;
  try {
    if (isEdit.value && props.id !== undefined) {
      const updated = await updateMovie(props.id, payload);
      fillForm(updated);
      successMessage.value = `Los cambios de «${updated.titulo}» se guardaron correctamente.`;
    } else {
      const created = await createMovie(payload);
      resetForm();
      successMessage.value = `«${created.titulo}» se añadió a la cartelera. Puedes registrar otra película.`;
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (error) {
    const { message, fields } = parseApiError(error, 'No se pudo guardar la película.');
    globalError.value = message;
    errors.value = fields as Partial<Record<Field, string>>;
    if (/sala/i.test(message)) errors.value.salaId = message;
    if (/imagen/i.test(message)) errors.value.imagen = message;
    if (/hora/i.test(message)) errors.value.horario = message;
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  try {
    const [roomList, movie] = await Promise.all([
      getRooms(),
      isEdit.value && props.id !== undefined ? getMovie(props.id) : Promise.resolve(null)
    ]);
    rooms.value = roomList;
    if (movie) fillForm(movie);
  } catch (error) {
    globalError.value = parseApiError(error, 'No se pudieron cargar los datos del formulario.').message;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="mx-auto max-w-5xl animate-fade-up">
    <RouterLink
      :to="isEdit ? { name: 'pelicula-detalle', params: { id } } : { name: 'peliculas' }"
      class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text"
    >
      ← {{ isEdit ? 'Volver al detalle' : 'Volver al catálogo' }}
    </RouterLink>
    <header class="mb-6">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Gestión de películas</p>
      <h1 class="text-3xl font-extrabold">{{ isEdit ? 'Editar película' : 'Añadir película' }}</h1>
    </header>

    <Transition name="fade">
      <div v-if="successMessage" class="venly-alert-success mb-6 flex flex-wrap items-center justify-between gap-3" role="status">
        <span>✅ {{ successMessage }}</span>
        <RouterLink to="/peliculas" class="font-semibold underline-offset-2 hover:underline">Ver cartelera →</RouterLink>
      </div>
    </Transition>
    <div v-if="globalError" class="venly-alert-error mb-6" role="alert">{{ globalError }}</div>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Cargando formulario…</div>

    <form v-else class="venly-card grid gap-8 p-6 sm:p-8 lg:grid-cols-[280px_1fr]" novalidate @submit.prevent="submit">
      <!-- Poster -->
      <div>
        <span class="venly-label">Póster (JPG o PNG, máx. 10 MB)</span>
        <div
          class="relative flex aspect-[2/3] cursor-pointer flex-col items-center justify-center overflow-hidden rounded-xl border-2 border-dashed text-center transition-colors focus-within:ring-2 focus-within:ring-venly-accent/50"
          :class="[
            dragOver ? 'border-venly-accent bg-venly-accent/10' : 'border-venly-border bg-venly-bg/50 hover:border-venly-accent/60',
            errors.imagen ? 'border-red-500/70' : ''
          ]"
          @click="fileInput?.click()"
          @dragover.prevent="dragOver = true"
          @dragleave.prevent="dragOver = false"
          @drop.prevent="onDrop"
        >
          <img v-if="previewSrc" :src="previewSrc" alt="Vista previa del póster" class="absolute inset-0 h-full w-full object-cover" />
          <div v-else class="px-4 text-venly-muted">
            <p class="text-4xl">🖼️</p>
            <p class="mt-2 text-sm font-medium text-venly-text">Arrastra una imagen o haz clic</p>
            <p class="mt-1 text-xs">JPG o PNG · hasta 10 MB</p>
          </div>
          <input ref="fileInput" type="file" accept="image/jpeg,image/png" class="sr-only" aria-label="Seleccionar póster" @change="onFileChange" />
        </div>
        <p class="venly-field-error">{{ errors.imagen }}</p>
        <p v-if="imageName" class="truncate text-xs text-venly-muted">{{ imageName }}</p>
        <div v-if="previewSrc" class="mt-2 flex gap-2">
          <button type="button" class="venly-btn-secondary flex-1 px-3 py-1.5 text-sm" @click="fileInput?.click()">Cambiar</button>
          <button type="button" class="venly-btn-secondary flex-1 px-3 py-1.5 text-sm hover:!border-red-500 hover:!text-red-400" @click="removeImage">Quitar</button>
        </div>
      </div>

      <!-- Fields -->
      <div class="grid grid-cols-1 gap-x-5 gap-y-1 sm:grid-cols-2">
        <div class="sm:col-span-2">
          <label for="titulo" class="venly-label">Título</label>
          <input id="titulo" v-model="form.titulo" type="text" maxlength="150" placeholder="Ej. Dune: Parte Dos" class="venly-input" :class="{ 'venly-input-error': errors.titulo }" />
          <p class="venly-field-error">{{ errors.titulo }}</p>
        </div>

        <fieldset class="sm:col-span-2">
          <legend class="venly-label">Horario</legend>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label for="horaInicio" class="mb-1 block text-xs text-venly-muted">Inicio</label>
              <input id="horaInicio" v-model="form.horaInicio" type="time" class="venly-input" :class="{ 'venly-input-error': errors.horario }" />
            </div>
            <div>
              <label for="horaFin" class="mb-1 block text-xs text-venly-muted">Fin</label>
              <input id="horaFin" v-model="form.horaFin" type="time" class="venly-input" :class="{ 'venly-input-error': errors.horario }" />
            </div>
          </div>
          <p class="venly-field-error">{{ errors.horario }}</p>
        </fieldset>

        <fieldset>
          <legend class="venly-label">Duración</legend>
          <div class="grid grid-cols-2 gap-3">
            <label class="relative block">
              <input v-model.number="form.horas" type="number" min="0" max="99" placeholder="0" class="venly-input pr-8" :class="{ 'venly-input-error': errors.duracion }" aria-label="Horas" @input="durationTouched = true" />
              <span class="pointer-events-none absolute inset-y-0 right-3 flex items-center text-sm text-venly-muted">h</span>
            </label>
            <label class="relative block">
              <input v-model.number="form.minutos" type="number" min="0" max="59" placeholder="0" class="venly-input pr-12" :class="{ 'venly-input-error': errors.duracion }" aria-label="Minutos" @input="durationTouched = true" />
              <span class="pointer-events-none absolute inset-y-0 right-3 flex items-center text-sm text-venly-muted">min</span>
            </label>
          </div>
          <p class="venly-field-error">{{ errors.duracion }}</p>
        </fieldset>

        <div>
          <label for="clasificacion" class="venly-label">Clasificación</label>
          <select id="clasificacion" v-model="form.clasificacion" class="venly-input" :class="{ 'venly-input-error': errors.clasificacion }">
            <option value="" disabled>Seleccione…</option>
            <option v-for="c in clasificaciones" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
          <p class="venly-field-error">{{ errors.clasificacion }}</p>
        </div>

        <div class="sm:col-span-2">
          <label for="sala" class="venly-label">Sala</label>
          <select id="sala" v-model="form.salaId" class="venly-input" :class="{ 'venly-input-error': errors.salaId }">
            <option :value="null" disabled>Seleccione una sala…</option>
            <option v-for="room in rooms" :key="room.id" :value="room.id">{{ room.nombre }} · {{ room.capacidad }} butacas</option>
          </select>
          <p class="venly-field-error">{{ errors.salaId }}</p>
        </div>

        <div class="sm:col-span-2">
          <label for="sinopsis" class="venly-label">Sinopsis</label>
          <textarea
            id="sinopsis"
            v-model="form.sinopsis"
            rows="6"
            :maxlength="SINOPSIS_MAX"
            placeholder="Resumen de la película…"
            class="venly-input resize-y"
            :class="{ 'venly-input-error': errors.sinopsis }"
          ></textarea>
          <div class="flex justify-between">
            <p class="venly-field-error">{{ errors.sinopsis }}</p>
            <span class="mt-1 text-xs text-venly-muted">{{ form.sinopsis.length }}/{{ SINOPSIS_MAX }}</span>
          </div>
        </div>

        <div class="flex flex-col-reverse gap-3 pt-3 sm:col-span-2 sm:flex-row sm:justify-end">
          <RouterLink to="/peliculas" class="venly-btn-secondary">Cancelar</RouterLink>
          <button type="submit" class="venly-btn-primary" :disabled="saving">
            <LoadingSpinner v-if="saving" />
            {{ saving ? 'Guardando…' : isEdit ? 'Guardar cambios' : 'Guardar película' }}
          </button>
        </div>
      </div>
    </form>
  </section>
</template>
