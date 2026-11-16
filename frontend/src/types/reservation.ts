export type SeatStatus = 'DISPONIBLE' | 'OCUPADO';

export interface Seat {
  codigo: string;
  estado: SeatStatus;
}

export interface SeatMap {
  peliculaId: number;
  peliculaTitulo: string;
  salaNombre: string;
  horario: string;
  filas: number;
  columnas: number;
  maxAsientosPorCompra: number;
  precioUnitario: number;
  asientos: Seat[];
}

export interface CreateReservationPayload {
  peliculaId: number;
  asientos: string[];
  titular: string;
  numeroTarjeta: string;
  vencimiento: string;
  cvv: string;
}

export interface Reservation {
  id: number;
  codigo: string;
  perfilId: number;
  peliculaId: number;
  peliculaTitulo: string;
  salaNombre: string;
  horario: string;
  asientos: string[];
  cantidad: number;
  precioUnitario: number;
  total: number;
  metodoPago: string;
  tarjetaUltimos4: string;
  estado: string;
  fechaCreacion: string;
}
