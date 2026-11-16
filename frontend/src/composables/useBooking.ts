import { ref } from 'vue';

// Module-level state: the seat selection kept in memory between the seat map and the payment view.
const peliculaId = ref<number | null>(null);
const asientos = ref<string[]>([]);

export function useBooking() {
  /** Stores the seats chosen in the seat map so the payment view can send them. */
  function setSelection(id: number, seats: string[]) {
    console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Almacenamiento de selección de asientos en memoria\nDe: useBooking.ts -> Hacia: useBooking.ts");
    peliculaId.value = id;
    asientos.value = [...seats];
  }

  /** Drops the in-memory selection (after a successful payment or a seat conflict). */
  function clearSelection() {
    console.log("\n[VENLY TRACE - FRONTEND] 🔵 Acción: Limpieza de selección de asientos en memoria\nDe: useBooking.ts -> Hacia: useBooking.ts");
    peliculaId.value = null;
    asientos.value = [];
  }

  return { peliculaId, asientos, setSelection, clearSelection };
}
