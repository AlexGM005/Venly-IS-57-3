export type Clasificacion = 'G' | 'PG' | 'PG-13' | 'R' | 'NC-17';

export type ImageType = 'image/jpeg' | 'image/png';

export interface Movie {
  id: number;
  titulo: string;
  horario: string;
  duracion: string;
  sinopsis: string;
  clasificacion: Clasificacion;
  imagenBase64: string | null;
  imagenTipo: ImageType | null;
  salaId: number;
  salaNombre: string;
}

export interface MoviePayload {
  titulo: string;
  horario: string;
  duracion: string;
  sinopsis: string;
  clasificacion: Clasificacion | '';
  salaId: number | null;
  imagenBase64?: string | null;
  imagenTipo?: ImageType | null;
  /** Only for edition: removes the current image when no new one is sent. */
  quitarImagen?: boolean;
}

export interface Room {
  id: number;
  nombre: string;
  capacidad: number;
}
