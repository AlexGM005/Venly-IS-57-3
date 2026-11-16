package com.venly.backend.service.impl;

import com.venly.backend.dto.SeatDto;
import com.venly.backend.dto.SeatMapResponse;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Movie;
import com.venly.backend.model.Room;
import com.venly.backend.repository.MovieRepository;
import com.venly.backend.repository.ReservationRepository;
import com.venly.backend.repository.RoomRepository;
import com.venly.backend.service.SeatService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SeatServiceImpl implements SeatService {

    static final int COLUMNAS = 10;
    static final int MAX_ASIENTOS_POR_COMPRA = 4;
    static final double PRECIO_UNITARIO = 8.00;
    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";
    private static final String DISPONIBLE = "DISPONIBLE";
    private static final String OCUPADO = "OCUPADO";

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final RequesterResolver requesterResolver;

    public SeatServiceImpl(MovieRepository movieRepository,
                           RoomRepository roomRepository,
                           ReservationRepository reservationRepository,
                           RequesterResolver requesterResolver) {
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.requesterResolver = requesterResolver;
    }

    @Override
    public SeatMapResponse getSeatMap(Long requesterId, Long peliculaId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta del mapa de asientos de la función\nDe: SeatServiceImpl.java -> Hacia: JsonReservationRepository.java");
        requesterResolver.require(requesterId);
        Movie movie = movieRepository.findById(peliculaId)
                .orElseThrow(() -> fail(HttpStatus.NOT_FOUND, "Consulta del mapa de asientos", "Película no encontrada"));
        Room room = roomRepository.findById(movie.getSalaId())
                .orElseThrow(() -> fail(HttpStatus.NOT_FOUND, "Consulta del mapa de asientos", "Sala no encontrada"));

        int capacidad = room.getCapacidad() == null ? 0 : room.getCapacidad();
        Set<String> ocupados = reservationRepository.findByPeliculaId(peliculaId).stream()
                .filter(r -> ESTADO_CONFIRMADA.equals(r.getEstado()))
                .filter(r -> r.getAsientos() != null)
                .flatMap(r -> r.getAsientos().stream())
                .collect(Collectors.toSet());

        SeatMapResponse response = new SeatMapResponse();
        response.setPeliculaId(movie.getId());
        response.setPeliculaTitulo(movie.getTitulo());
        response.setSalaNombre(room.getNombre());
        response.setHorario(movie.getHorario());
        response.setFilas(filas(capacidad));
        response.setColumnas(COLUMNAS);
        response.setMaxAsientosPorCompra(MAX_ASIENTOS_POR_COMPRA);
        response.setPrecioUnitario(PRECIO_UNITARIO);
        response.setAsientos(buildSeats(capacidad, ocupados));
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Consulta del mapa de asientos de la función\nDe: SeatServiceImpl.java -> Hacia: SeatController.java\nEstado: 200 (Éxito - Mapa de asientos generado)");
        return response;
    }

    private static int filas(int capacidad) {
        return (int) Math.ceil(capacidad / (double) COLUMNAS);
    }

    /** Builds "A1".."A10", "B1".. and so on, never exceeding the room capacity. */
    private static List<SeatDto> buildSeats(int capacidad, Set<String> ocupados) {
        List<SeatDto> asientos = new ArrayList<>();
        int generados = 0;
        for (int fila = 0; fila < filas(capacidad) && generados < capacidad; fila++) {
            char letra = (char) ('A' + fila);
            for (int columna = 1; columna <= COLUMNAS && generados < capacidad; columna++) {
                String codigo = letra + String.valueOf(columna);
                asientos.add(new SeatDto(codigo, ocupados.contains(codigo) ? OCUPADO : DISPONIBLE));
                generados++;
            }
        }
        return asientos;
    }

    private static BusinessException fail(HttpStatus status, String traza, String mensaje) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: " + traza
                + "\nDe: SeatServiceImpl.java\nEstado: " + status.value()
                + " (" + motivo(status) + " - " + mensaje + ")");
        return new BusinessException(status, mensaje);
    }

    private static String motivo(HttpStatus status) {
        return switch (status.value()) {
            case 201 -> "Creado";
            case 400 -> "Solicitud Incorrecta";
            case 402 -> "Pago Requerido";
            case 403 -> "Prohibido";
            case 404 -> "No Encontrado";
            case 409 -> "Conflicto";
            default -> status.getReasonPhrase();
        };
    }
}
