<script setup lang="ts">
import { onMounted, ref } from 'vue';
import LoadingSpinner from '@/components/LoadingSpinner.vue';
import { parseApiError, getReservation } from '@/services/venlyService';
import type { Reservation } from '@/types/reservation';

const props = defineProps<{ id: number }>();

const reservation = ref<Reservation | null>(null);
const loading = ref(true);
const error = ref('');

function formatMoney(value: number): string {
  return `$${value.toFixed(2)}`;
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('es-VE', { dateStyle: 'long', timeStyle: 'short' }).format(date);
}

function printReceipt() {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Impresión de comprobante de reserva\nDe: ReservationDetailView.vue -> Hacia: window.print");
  window.print();
}

onMounted(async () => {
  console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Consulta de reserva por id (GET)\nDe: ReservationDetailView.vue -> Hacia: reservationService.ts");
  try {
    reservation.value = await getReservation(props.id);
    console.log("\n[VENLY TRACE - FRONTEND] 🟢 Acción: Comprobante de reserva cargado\nDe: ReservationDetailView.vue -> Hacia: reservationService.ts\nEstado: 200 (Éxito - Reserva obtenida correctamente)");
  } catch (e) {
    error.value = parseApiError(e, 'No se pudo cargar la reserva.').message;
    console.log("\n[VENLY TRACE - FRONTEND] 🔴 Acción: Error al consultar la reserva\nDe: ReservationDetailView.vue -> Hacia: reservationService.ts\nEstado: 400 (Solicitud Incorrecta - No se pudo obtener la reserva)");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="mx-auto max-w-3xl animate-fade-up">
    <RouterLink :to="{ name: 'reservas' }" class="mb-6 inline-flex items-center gap-1 text-sm text-venly-muted hover:text-venly-text">← Volver a las reservas</RouterLink>

    <div v-if="loading" class="venly-card flex items-center justify-center gap-3 p-12 text-venly-muted"><LoadingSpinner /> Cargando reserva...</div>
    <div v-else-if="error" class="venly-alert-error" role="alert">{{ error }}</div>

    <article v-else-if="reservation" class="venly-card overflow-hidden">
      <header class="border-b border-venly-border bg-venly-bg/40 p-6 sm:p-8">
        <p class="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-venly-gold">Comprobante de reserva</p>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <h1 class="text-2xl font-extrabold sm:text-3xl">{{ reservation.codigo }}</h1>
          <span class="rounded-md bg-emerald-500/15 px-3 py-1 text-sm font-semibold text-emerald-300 ring-1 ring-emerald-500/40">{{ reservation.estado }}</span>
        </div>
        <p class="mt-2 text-sm text-venly-muted">Emitido el {{ formatDate(reservation.fechaCreacion) }}</p>
      </header>

      <div class="p-6 sm:p-8">
        <dl class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">🎬 Película</dt>
            <dd class="mt-1 font-semibold">{{ reservation.peliculaTitulo }}</dd>
          </div>

            <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">👤 Perfil</dt>
            <dd class="mt-1 font-semibold">{{ reservation.perfilId }}</dd>
          </div>

          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">📍 Sala</dt>
            <dd class="mt-1 font-semibold">{{ reservation.salaNombre }}</dd>
          </div>
          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">🕒 Horario</dt>
            <dd class="mt-1 font-semibold">{{ reservation.horario }}</dd>
          </div>
          <div class="rounded-xl border border-venly-border bg-venly-bg/50 p-4">
            <dt class="text-xs uppercase tracking-wider text-venly-muted">💳 Tarjeta</dt>
            <dd class="mt-1 font-semibold">**** {{ reservation.tarjetaUltimos4 }}</dd>
          </div>
        </dl>

        <div class="mt-6 rounded-xl border border-venly-border bg-venly-bg/50 p-4">
          <p class="text-xs uppercase tracking-wider text-venly-muted">Asientos ({{ reservation.cantidad }})</p>
          <div class="mt-2 flex flex-wrap gap-1.5">
            <span v-for="codigo in reservation.asientos" :key="codigo" class="rounded-md bg-venly-accent/20 px-2 py-0.5 text-sm font-semibold text-venly-accent ring-1 ring-venly-accent/40">{{ codigo }}</span>
          </div>
        </div>

        <div class="mt-6 flex flex-wrap items-center justify-between gap-3 border-t border-venly-border pt-4">
          <div class="text-sm text-venly-muted">
            {{ reservation.cantidad }} × {{ formatMoney(reservation.precioUnitario) }} · {{ reservation.metodoPago }}
          </div>
          <div class="text-right">
            <p class="text-xs uppercase tracking-wider text-venly-muted">Total</p>
            <p class="text-2xl font-extrabold text-venly-gold">{{ formatMoney(reservation.total) }}</p>
          </div>
        </div>

        <div class="mt-8 flex flex-wrap gap-3">
          <!-- <button type="button" class="venly-btn-primary" @click="printReceipt">🖨️ Imprimir</button> -->
          <RouterLink :to="{ name: 'reservas' }" class="venly-btn-secondary">Volver</RouterLink>
        </div>
      </div>
    </article>
  </section>
</template>
