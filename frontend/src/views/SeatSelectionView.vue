<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useBooking } from '@/composables/useBooking';
import { useSession } from '@/composables/useSession';
import { parseApiError } from '@/services/httpClient';
import { getSeatMap } from '@/services/seatService';
import type { Seat, SeatMap } from '@/types/reservation';

const props = defineProps<{ id: number }>();
const router = useRouter();
const { isStaff } = useSession();
const { setSelection } = useBooking();

const MAX_ASIENTOS = 4;

interface SeatRow {
  letra: string;
  asientos: Seat[];
}

const seatMap = ref<SeatMap | null>(null);
const loading = ref(true);
const error = ref('');
const selected = ref<string[]>([]);
const selectionMessage = ref('');

const rows = computed<SeatRow[]>(() => {
  const grouped = new Map<string, Seat[]>();
  for (const seat of seatMap.value?.asientos ?? []) {
    const letra = seat.codigo.replace(/[0-9]/g, '');
    const list = grouped.get(letra);
    if (list) list.push(seat);
    else grouped.set(letra, [seat]);
  }
  return [...grouped.entries()].map(([letra, asientos]) => ({ letra, asientos }));
});

const disponibles = computed(() => (seatMap.value?.asientos ?? []).filter((s) => s.estado === 'DISPONIBLE').length);
const ocupados = computed(() => (seatMap.value?.asientos ?? []).filter((s) => s.estado === 'OCUPADO').length);
const total = computed(() => selected.value.length * (seatMap.value?.precioUnitario ?? 0));

function formatMoney(value: number): string {
  return `$${value.toFixed(2)}`;
}

function seatClasses(seat: Seat): string {
  if (seat.estado === 'OCUPADO') return 'border-venly-border/40 bg-venly-card/40 text-gray-600 cursor-not-allowed';
  if (selected.value.includes(seat.codigo)) return 'border-venly-accent bg-venly-accent text-white shadow-glow';
  return 'border-venly-border bg-venly-bg/70 text-venly-muted hover:border-venly-accent hover:text-venly-accent';
}

function toggleSeat(seat: Seat) {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Selección de asientos\nDe: SeatSelectionView.vue -> Hacia: httpClient.ts");
  if (isStaff.value || seat.estado === 'OCUPADO') return;

  const index = selected.value.indexOf(seat.codigo);
  if (index >= 0) {
    selected.value.splice(index, 1);
    selectionMessage.value = '';
    return;
  }
  if (selected.value.length >= MAX_ASIENTOS) {
    selectionMessage.value = `Solo puedes seleccionar hasta ${MAX_ASIENTOS} asientos`;
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Límite de asientos por compra alcanzado\nDe: SeatSelectionView.vue -> Hacia: SeatSelectionView.vue\nEstado: 400 (Solicitud Incorrecta - Máximo de asientos excedido)");
    return;
  }
  selected.value.push(seat.codigo);
  selectionMessage.value = '';
}

function goToPayment() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Continuar al pago\nDe: SeatSelectionView.vue -> Hacia: useBooking.ts");
  if (selected.value.length === 0) {
    selectionMessage.value = 'Selecciona al menos un asiento para continuar';
    return;
  }
  setSelection(props.id, selected.value);
  router.push({ name: 'pago', params: { id: props.id } });
}

onMounted(async () => {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Solicitud de mapa de asientos\nDe: SeatSelectionView.vue -> Hacia: seatService.ts");
  try {
    seatMap.value = await getSeatMap(props.id);
    console.log("\n[VENLY TRACE - FRONTEND] 🟢 Acción: Mapa de asientos cargado\nDe: SeatSelectionView.vue -> Hacia: seatService.ts\nEstado: 200 (Éxito - Asientos disponibles y ocupados obtenidos)");
  } catch (e) {
    error.value = parseApiError(e, 'No se pudo cargar el mapa de asientos.').message;
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Error al cargar mapa de asientos\nDe: SeatSelectionView.vue -> Hacia: seatService.ts\nEstado: 400 (Solicitud Incorrecta - No se pudo obtener el mapa de asientos)");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="mx-auto max-w-5xl animate-fade-up">
    <RouterLink :to="{ name: 'pelicula-detalle', params: { id } }" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver al detalle</RouterLink>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Cargando asientos...</div>
    <div v-else-if="error" class="venly-alert-error" role="alert">{{ error }}</div>

    <template v-else-if="seatMap">
      <header class="mb-6">
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">{{ isStaff ? 'Consulta de asientos' : 'Selección de asientos' }}</p>
        <h1 class="text-3xl font-extrabold">{{ seatMap.peliculaTitulo }}</h1>
        <p class="mt-1 flex flex-wrap gap-x-4 gap-y-1 text-sm text-venly-muted">
          <span>📍 {{ seatMap.salaNombre }}</span>
          <span>🕒 {{ seatMap.horario }}</span>
          <span>💵 {{ formatMoney(seatMap.precioUnitario) }} por asiento</span>
        </p>
      </header>

      

      <div class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_280px]">
        <div class="venly-card p-5 sm:p-7">
          <div class="mx-auto mb-6 max-w-md">
            <p class="rounded-t-2xl border border-venly-border bg-gradient-to-b from-venly-card to-venly-bg/60 py-2 text-center text-xs uppercase tracking-[0.4em] text-venly-muted">Pantalla</p>
            <div class="h-2 rounded-b-full bg-gradient-to-r from-transparent via-venly-accent/70 to-transparent"></div>
          </div>

          <div class="space-y-2 overflow-x-auto">
            <div v-for="row in rows" :key="row.letra" class="flex items-center justify-center gap-2">
              <span class="w-5 shrink-0 text-center text-xs font-semibold text-venly-muted">{{ row.letra }}</span>
              <button
                v-for="seat in row.asientos"
                :key="seat.codigo"
                type="button"
                class="h-8 w-8 shrink-0 rounded-md border text-[10px] font-semibold transition-transform sm:h-9 sm:w-9 sm:text-xs"
                :class="seatClasses(seat)"
                :disabled="isStaff || seat.estado === 'OCUPADO'"
                :aria-pressed="selected.includes(seat.codigo)"
                :aria-label="`Asiento ${seat.codigo} ${seat.estado.toLowerCase()}`"
                :title="`${seat.codigo} · ${seat.estado}`"
                @click="toggleSeat(seat)"
              >
                {{ seat.codigo }}
              </button>
            </div>
          </div>

          <ul class="mt-7 flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-xs text-venly-muted">
            <li class="flex items-center gap-2"><span class="h-4 w-4 rounded border border-venly-border bg-venly-bg/70"></span> Disponible</li>
            <li class="flex items-center gap-2"><span class="h-4 w-4 rounded border border-venly-accent bg-venly-accent"></span> Seleccionado</li>
            <li class="flex items-center gap-2"><span class="h-4 w-4 rounded border border-venly-border/40 bg-venly-card/40"></span> Ocupado</li>
          </ul>
        </div>

        <aside class="venly-card h-fit p-5">
          <h2 class="text-sm font-semibold uppercase tracking-wider text-venly-gold">Resumen</h2>

          <dl class="mt-4 space-y-2 text-sm">
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Disponibles</dt>
              <dd class="font-semibold text-emerald-300">{{ disponibles }}</dd>
            </div>
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Ocupados</dt>
              <dd class="font-semibold text-venly-accent">{{ ocupados }}</dd>
            </div>
            <div class="flex items-center justify-between gap-3">
              <dt class="text-venly-muted">Límite por compra</dt>
              <dd class="font-semibold">{{ MAX_ASIENTOS }}</dd>
            </div>
          </dl>

          <div class="mt-5 border-t border-venly-border pt-4">
            <p class="text-xs uppercase tracking-wider text-venly-muted">Asientos seleccionados</p>
            <p v-if="selected.length === 0" class="mt-2 text-sm text-venly-muted">Ninguno todavía</p>
            <div v-else class="mt-2 flex flex-wrap gap-1.5">
              <span v-for="codigo in selected" :key="codigo" class="rounded-md bg-venly-accent/20 px-2 py-0.5 text-xs font-semibold text-venly-accent ring-1 ring-venly-accent/40">{{ codigo }}</span>
            </div>
          </div>

          <div class="mt-5 flex items-center justify-between border-t border-venly-border pt-4">
            <span class="text-sm text-venly-muted">Total</span>
            <span class="text-xl font-extrabold text-venly-gold">{{ formatMoney(total) }}</span>
          </div>

          <p v-if="selectionMessage" class="mt-3 text-xs text-red-400" role="alert">{{ selectionMessage }}</p>

          <button v-if="!isStaff" type="button" class="venly-btn-primary mt-5 w-full" :disabled="selected.length === 0" @click="goToPayment">
            Continuar al pago
          </button>
        </aside>
      </div>
    </template>
  </section>
</template>
