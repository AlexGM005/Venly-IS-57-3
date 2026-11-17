<script setup lang="ts">
import { onMounted, ref } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { useSession } from '@/composables/useSession';
import { parseApiError, listReservations } from '@/services/venlyService';
import type { Reservation } from '@/types/reservation';

const { isStaff } = useSession();

const reservations = ref<Reservation[]>([]);
const loading = ref(true);
const error = ref('');

function formatMoney(value: number): string {
  return `$${value.toFixed(2)}`;
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('es-VE', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

async function load() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de reservas (GET)\nDe: ReservationListView.vue -> Hacia: reservationService.ts");
  loading.value = true;
  error.value = '';
  try {
    reservations.value = await listReservations();
    console.log("\n[VENLY TRACE - FRONTEND] 🟢 Acción: Listado de reservas cargado\nDe: ReservationListView.vue -> Hacia: reservationService.ts\nEstado: 200 (Éxito - Reservas obtenidas correctamente)");
  } catch (e) {
    error.value = parseApiError(e, 'No se pudieron cargar las reservas.').message;
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Error al consultar las reservas\nDe: ReservationListView.vue -> Hacia: reservationService.ts\nEstado: 400 (Solicitud Incorrecta - No se pudieron obtener las reservas)");
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="mx-auto max-w-5xl animate-fade-up">
    <RouterLink to="/menu" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver al menú</RouterLink>

    <header class="mb-8">
      <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">{{ isStaff ? 'Reservas' : 'Mis reservas' }}</p>
      <h1 class="text-3xl font-extrabold">{{ isStaff ? 'Reservas registradas' : 'Mis reservas' }}</h1>
      <p class="mt-1 text-sm text-venly-muted">
        {{ reservations.length }} {{ reservations.length === 1 ? 'reserva' : 'reservas' }}
        {{ isStaff ? 'en el sistema' : 'a tu nombre' }}
      </p>
    </header>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Cargando reservas...</div>
    <div v-else-if="error" class="venly-alert-error flex items-center justify-between gap-4" role="alert">
      {{ error }}
      <button type="button" class="venly-btn-secondary py-1.5" @click="load">Reintentar</button>
    </div>
    <div v-else-if="reservations.length === 0" class="venly-card p-12 text-center">
      <p class="text-5xl">🎟️</p>
      <p class="mt-3 text-lg font-semibold">Aún no hay reservas</p>
      <p class="mt-1 text-sm text-venly-muted">{{ isStaff ? 'Las compras de los clientes aparecerán aquí.' : 'Compra tus entradas desde la cartelera.' }}</p>
      <RouterLink v-if="!isStaff" to="/peliculas" class="venly-btn-primary mt-5">Ver cartelera</RouterLink>
    </div>

    <ul v-else class="space-y-3">
      <li v-for="reservation in reservations" :key="reservation.id">
        <RouterLink
          :to="{ name: 'reserva-detalle', params: { id: reservation.id } }"
          class="venly-card flex flex-col gap-3 p-5 hover:-translate-y-0.5 hover:border-venly-accent/60 hover:shadow-glow sm:flex-row sm:items-center sm:justify-between"
        >
          <div class="min-w-0">
            <p class="text-xs uppercase tracking-wider text-venly-gold">{{ reservation.codigo }}</p>
            <p class="mt-1 truncate text-lg font-bold">{{ reservation.peliculaTitulo }}</p>
            <p class="mt-1 flex flex-wrap gap-x-3 gap-y-1 text-xs text-venly-muted">
              <span>📍 {{ reservation.salaNombre }}</span>
              <span>🕒 {{ reservation.horario }}</span>
              <span>🎟️ {{ reservation.asientos.join(', ') }}</span>
              <span>📅 {{ formatDate(reservation.fechaCreacion) }}</span>
            </p>
          </div>
          <div class="flex items-center gap-4 sm:flex-col sm:items-end">
            <span class="rounded-md bg-emerald-500/15 px-2.5 py-0.5 text-xs font-semibold text-emerald-300 ring-1 ring-emerald-500/40">{{ reservation.estado }}</span>
            <span class="text-lg font-extrabold text-venly-gold">{{ formatMoney(reservation.total) }}</span>
          </div>
        </RouterLink>
      </li>
    </ul>
  </section>
</template>
