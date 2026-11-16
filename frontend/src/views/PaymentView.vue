<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useBooking } from '@/composables/useBooking';
import { useSession } from '@/composables/useSession';
import { HttpError, parseApiError } from '@/services/httpClient';
import { createReservation } from '@/services/reservationService';
import { getSeatMap } from '@/services/seatService';
import type { SeatMap } from '@/types/reservation';

const props = defineProps<{ id: number }>();
const router = useRouter();
const { isStaff } = useSession();
const { asientos, clearSelection } = useBooking();

const seatMap = ref<SeatMap | null>(null);
const loading = ref(true);
const loadError = ref('');
const submitting = ref(false);
const errorMessage = ref('');
const fieldErrors = ref<Record<string, string>>({});

const form = ref({ titular: '', numeroTarjeta: '', vencimiento: '', cvv: '' });

const total = computed(() => asientos.value.length * (seatMap.value?.precioUnitario ?? 0));

function formatMoney(value: number): string {
  return `$${value.toFixed(2)}`;
}

function onCardInput(event: Event) {
  const input = event.target as HTMLInputElement;
  const digits = input.value.replace(/\D/g, '').slice(0, 16);
  form.value.numeroTarjeta = digits.replace(/(\d{4})(?=\d)/g, '$1 ');
  input.value = form.value.numeroTarjeta;
}

function onExpiryInput(event: Event) {
  const input = event.target as HTMLInputElement;
  const digits = input.value.replace(/\D/g, '').slice(0, 4);
  form.value.vencimiento = digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
  input.value = form.value.vencimiento;
}

function onCvvInput(event: Event) {
  const input = event.target as HTMLInputElement;
  const digits = input.value.replace(/\D/g, '').slice(0, 4);
  form.value.cvv = digits;
  input.value = digits;
}

function validate(): boolean {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Validación de formulario de pago\nDe: PaymentView.vue -> Hacia: PaymentView.vue");
  const next: Record<string, string> = {};
  if (!form.value.titular.trim()) next.titular = 'El titular de la tarjeta es obligatorio';
  if (form.value.numeroTarjeta.replace(/\s/g, '').length !== 16) next.numeroTarjeta = 'El número de tarjeta debe tener 16 dígitos';
  if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(form.value.vencimiento)) next.vencimiento = 'El vencimiento debe tener el formato MM/AA';
  if (!/^\d{3,4}$/.test(form.value.cvv)) next.cvv = 'El CVV debe tener 3 o 4 dígitos';
  fieldErrors.value = next;
  return Object.keys(next).length === 0;
}

async function submit() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Pago simulado de reserva (POST)\nDe: PaymentView.vue -> Hacia: reservationService.ts");
  errorMessage.value = '';
  if (!validate()) {
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Formulario de pago inválido\nDe: PaymentView.vue -> Hacia: PaymentView.vue\nEstado: 400 (Solicitud Incorrecta - Datos de tarjeta inválidos)");
    return;
  }

  submitting.value = true;
  try {
    const reservation = await createReservation({
      peliculaId: props.id,
      asientos: [...asientos.value],
      titular: form.value.titular.trim(),
      numeroTarjeta: form.value.numeroTarjeta.replace(/\s/g, ''),
      vencimiento: form.value.vencimiento,
      cvv: form.value.cvv
    });
    clearSelection();
    console.log("\n[VENLY TRACE - FRONTEND] 🟢 Acción: Reserva registrada\nDe: PaymentView.vue -> Hacia: reservationService.ts\nEstado: 201 (Creado - Reserva confirmada correctamente)");
    router.push({ name: 'reserva-detalle', params: { id: reservation.id } });
  } catch (e) {
    const parsed = parseApiError(e, 'No se pudo completar la reserva.');
    const status = e instanceof HttpError ? e.status : 0;
    errorMessage.value = parsed.message;
    fieldErrors.value = parsed.fields;

    if (status === 402) {
      console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Pago simulado rechazado\nDe: PaymentView.vue -> Hacia: reservationService.ts\nEstado: 402 (Pago Requerido - Pago simulado rechazado)");
    } else if (status === 409) {
      console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Asiento ya ocupado\nDe: PaymentView.vue -> Hacia: reservationService.ts\nEstado: 409 (Conflicto - Asiento ya ocupado)");
      clearSelection();
      window.setTimeout(() => router.push({ name: 'seleccion-asientos', params: { id: props.id } }), 1500);
    } else if (status === 403) {
      console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Reserva no permitida para el rol\nDe: PaymentView.vue -> Hacia: reservationService.ts\nEstado: 403 (Prohibido - Solo un CLIENTE puede reservar)");
    } else {
      console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Error al registrar la reserva\nDe: PaymentView.vue -> Hacia: reservationService.ts\nEstado: 400 (Solicitud Incorrecta - Datos de reserva inválidos)");
    }
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Preparación del pago simulado\nDe: PaymentView.vue -> Hacia: useBooking.ts");
  if (isStaff.value) {
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Acceso al pago denegado\nDe: PaymentView.vue -> Hacia: router/index.ts\nEstado: 403 (Prohibido - Solo un CLIENTE puede pagar)");
    router.replace({ name: 'menu' });
    return;
  }
  if (asientos.value.length === 0) {
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Pago sin asientos seleccionados\nDe: PaymentView.vue -> Hacia: router/index.ts\nEstado: 400 (Solicitud Incorrecta - No hay asientos seleccionados)");
    router.replace({ name: 'seleccion-asientos', params: { id: props.id } });
    return;
  }
  try {
    seatMap.value = await getSeatMap(props.id);
    console.log("\n[VENLY TRACE - FRONTEND] 🟢 Acción: Resumen de función cargado\nDe: PaymentView.vue -> Hacia: seatService.ts\nEstado: 200 (Éxito - Datos de la función obtenidos)");
  } catch (e) {
    loadError.value = parseApiError(e, 'No se pudo cargar el resumen de la función.').message;
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Error al cargar el resumen de la función\nDe: PaymentView.vue -> Hacia: seatService.ts\nEstado: 400 (Solicitud Incorrecta - No se pudo obtener la función)");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="mx-auto max-w-4xl animate-fade-up">
    <RouterLink :to="{ name: 'seleccion-asientos', params: { id } }" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver a los asientos</RouterLink>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Preparando el pago...</div>
    <div v-else-if="loadError" class="venly-alert-error" role="alert">{{ loadError }}</div>

    <template v-else-if="seatMap">
      <header class="mb-6">
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Pago simulado</p>
        <h1 class="text-3xl font-extrabold">Confirma tu compra</h1>
        <p class="mt-1 text-sm text-venly-muted">{{ seatMap.peliculaTitulo }} · {{ seatMap.salaNombre }} · {{ seatMap.horario }}</p>
      </header>

      <div class="mb-6 rounded-xl border border-venly-gold/40 bg-venly-gold/10 p-4 text-sm text-venly-gold" role="status">
        <p class="font-semibold">Modo prueba: no se realiza ningún cobro real</p>
        <p class="mt-1 text-venly-gold/80">Usa 4242 4242 4242 4242 para un pago aprobado. Una tarjeta terminada en 0000 simula un rechazo.</p>
      </div>

      <div class="grid gap-6 md:grid-cols-[minmax(0,1fr)_260px]">
        <form class="venly-card p-6" novalidate @submit.prevent="submit">
          <h2 class="text-sm font-semibold uppercase tracking-wider text-venly-gold">Tarjeta de prueba</h2>

          <div class="mt-4">
            <label class="venly-label" for="titular">Titular</label>
            <input
              id="titular"
              v-model="form.titular"
              type="text"
              class="venly-input"
              :class="{ 'venly-input-error': fieldErrors.titular }"
              placeholder="Nombre como aparece en la tarjeta"
              autocomplete="cc-name"
            />
            <p class="venly-field-error">{{ fieldErrors.titular }}</p>
          </div>

          <div class="mt-2">
            <label class="venly-label" for="numeroTarjeta">Número de tarjeta</label>
            <input
              id="numeroTarjeta"
              :value="form.numeroTarjeta"
              type="text"
              inputmode="numeric"
              class="venly-input"
              :class="{ 'venly-input-error': fieldErrors.numeroTarjeta }"
              placeholder="4242 4242 4242 4242"
              autocomplete="cc-number"
              @input="onCardInput"
            />
            <p class="venly-field-error">{{ fieldErrors.numeroTarjeta }}</p>
          </div>

          <div class="mt-2 grid gap-4 sm:grid-cols-2">
            <div>
              <label class="venly-label" for="vencimiento">Vencimiento</label>
              <input
                id="vencimiento"
                :value="form.vencimiento"
                type="text"
                inputmode="numeric"
                class="venly-input"
                :class="{ 'venly-input-error': fieldErrors.vencimiento }"
                placeholder="MM/AA"
                autocomplete="cc-exp"
                @input="onExpiryInput"
              />
              <p class="venly-field-error">{{ fieldErrors.vencimiento }}</p>
            </div>
            <div>
              <label class="venly-label" for="cvv">CVV</label>
              <input
                id="cvv"
                :value="form.cvv"
                type="password"
                inputmode="numeric"
                class="venly-input"
                :class="{ 'venly-input-error': fieldErrors.cvv }"
                placeholder="123"
                autocomplete="cc-csc"
                @input="onCvvInput"
              />
              <p class="venly-field-error">{{ fieldErrors.cvv }}</p>
            </div>
          </div>

          <div v-if="errorMessage" class="venly-alert-error mt-4" role="alert">{{ errorMessage }}</div>

          <button type="submit" class="venly-btn-primary mt-6 w-full" :disabled="submitting">
            <LoadingSpinner v-if="submitting" />
            {{ submitting ? 'Procesando pago...' : `Pagar ${formatMoney(total)}` }}
          </button>
        </form>

        <aside class="venly-card h-fit p-5">
          <h2 class="text-sm font-semibold uppercase tracking-wider text-venly-gold">Resumen</h2>
          <dl class="mt-4 space-y-2 text-sm">
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Película</dt>
              <dd class="text-right font-semibold">{{ seatMap.peliculaTitulo }}</dd>
            </div>
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Sala</dt>
              <dd class="text-right font-semibold">{{ seatMap.salaNombre }}</dd>
            </div>
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Horario</dt>
              <dd class="text-right font-semibold">{{ seatMap.horario }}</dd>
            </div>
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Precio unitario</dt>
              <dd class="text-right font-semibold">{{ formatMoney(seatMap.precioUnitario) }}</dd>
            </div>
          </dl>

          <div class="mt-4 border-t border-venly-border pt-4">
            <p class="text-xs uppercase tracking-wider text-venly-muted">Asientos</p>
            <div class="mt-2 flex flex-wrap gap-1.5">
              <span v-for="codigo in asientos" :key="codigo" class="rounded-md bg-venly-accent/20 px-2 py-0.5 text-xs font-semibold text-venly-accent ring-1 ring-venly-accent/40">{{ codigo }}</span>
            </div>
          </div>

          <div class="mt-4 flex items-center justify-between border-t border-venly-border pt-4">
            <span class="text-sm text-venly-muted">Total</span>
            <span class="text-xl font-extrabold text-venly-gold">{{ formatMoney(total) }}</span>
          </div>
        </aside>
      </div>
    </template>
  </section>
</template>
