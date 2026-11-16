package com.venly.backend.service.impl;

import com.venly.backend.dto.CreateReservationRequest;
import com.venly.backend.dto.ReservationResponse;
import com.venly.backend.dto.SeatDto;
import com.venly.backend.dto.SeatMapResponse;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Profile;
import com.venly.backend.model.Reservation;
import com.venly.backend.model.Role;
import com.venly.backend.repository.ReservationRepository;
import com.venly.backend.service.ReservationService;
import com.venly.backend.service.SeatService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final String METODO_PAGO = "TARJETA_SIMULADA";
    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";
    private static final String PREFIJO_CODIGO = "VEN-";
    private static final String ALFANUMERICO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_CODIGO = 8;
    private static final String TARJETA_RECHAZADA = "0000";
    private static final String VENCIMIENTO_PATTERN = "^(0[1-9]|1[0-2])/\\d{2}$";
    private static final String NUMERO_TARJETA_PATTERN = "\\d{16}";
    private static final String CVV_PATTERN = "\\d{3,4}";

    private final ReservationRepository reservationRepository;
    private final SeatService seatService;
    private final RequesterResolver requesterResolver;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  SeatService seatService,
                                  RequesterResolver requesterResolver) {
        this.reservationRepository = reservationRepository;
        this.seatService = seatService;
        this.requesterResolver = requesterResolver;
    }

    @Override
    public ReservationResponse create(Long requesterId, CreateReservationRequest request) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Recepción de la solicitud de reserva\nDe: ReservationServiceImpl.java -> Hacia: RequesterResolver.java");
        Profile requester = requesterResolver.require(requesterId);
        if (requester.getRol() != Role.CLIENTE) {
            throw fail(HttpStatus.FORBIDDEN, "Verificación de rol para reservar", "Solo un cliente puede realizar reservaciones");
        }

        List<String> asientos = request.getAsientos();
        if (asientos.size() > SeatServiceImpl.MAX_ASIENTOS_POR_COMPRA) {
            throw fail(HttpStatus.BAD_REQUEST, "Validación de asientos", "Solo puedes seleccionar hasta 4 asientos");
        }
        if (asientos.isEmpty()) {
            throw fail(HttpStatus.BAD_REQUEST, "Validación de asientos", "Debes seleccionar al menos un asiento");
        }
        if (new HashSet<>(asientos).size() != asientos.size()) {
            throw fail(HttpStatus.BAD_REQUEST, "Validación de asientos", "No se permiten asientos duplicados en la misma reserva");
        }

        SeatMapResponse mapa = seatService.getSeatMap(requesterId, request.getPeliculaId());
        Set<String> codigosValidos = mapa.getAsientos().stream()
                .map(SeatDto::getCodigo)
                .collect(Collectors.toSet());
        for (String asiento : asientos) {
            if (!codigosValidos.contains(asiento)) {
                throw fail(HttpStatus.BAD_REQUEST, "Validación de asientos",
                        "El asiento " + asiento + " no existe en la sala de esta función");
            }
        }

        String numeroTarjeta = request.getNumeroTarjeta().replace(" ", "").trim();
        if (!numeroTarjeta.matches(NUMERO_TARJETA_PATTERN)) {
            throw fail(HttpStatus.BAD_REQUEST, "Validación de la tarjeta simulada", "El número de tarjeta debe tener 16 dígitos");
        }
        if (!request.getVencimiento().trim().matches(VENCIMIENTO_PATTERN)) {
            throw fail(HttpStatus.BAD_REQUEST, "Validación de la tarjeta simulada", "El vencimiento debe tener el formato MM/AA");
        }
        if (!request.getCvv().trim().matches(CVV_PATTERN)) {
            throw fail(HttpStatus.BAD_REQUEST, "Validación de la tarjeta simulada", "El CVV debe tener 3 o 4 dígitos");
        }

        String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        if (TARJETA_RECHAZADA.equals(ultimos4)) {
            throw fail(HttpStatus.PAYMENT_REQUIRED, "Procesando pago simulado", "Pago simulado rechazado (tarjeta de prueba)");
        }
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Procesando pago simulado\nDe: ReservationServiceImpl.java -> Hacia: Base.json\nEstado: 200 (Éxito - Pago simulado procesado y reserva guardada)");

        Reservation reservation = new Reservation();
        reservation.setCodigo(generarCodigo());
        reservation.setPerfilId(requester.getId());
        reservation.setPeliculaId(mapa.getPeliculaId());
        reservation.setPeliculaTitulo(mapa.getPeliculaTitulo());
        reservation.setSalaNombre(mapa.getSalaNombre());
        reservation.setHorario(mapa.getHorario());
        reservation.setAsientos(List.copyOf(asientos));
        reservation.setCantidad(asientos.size());
        reservation.setPrecioUnitario(SeatServiceImpl.PRECIO_UNITARIO);
        reservation.setTotal(SeatServiceImpl.PRECIO_UNITARIO * asientos.size());
        reservation.setMetodoPago(METODO_PAGO);
        reservation.setTarjetaUltimos4(ultimos4);
        reservation.setEstado(ESTADO_CONFIRMADA);
        reservation.setFechaCreacion(LocalDateTime.now().toString());

        Reservation saved = reservationRepository.saveIfSeatsAvailable(reservation);
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Confirmación de la reserva\nDe: ReservationServiceImpl.java -> Hacia: ReservationController.java\nEstado: 201 (Creado - Reserva " + saved.getCodigo() + " confirmada)");
        return toResponse(saved);
    }

    @Override
    public ReservationResponse findById(Long requesterId, Long reservaId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de reserva por id\nDe: ReservationServiceImpl.java -> Hacia: JsonReservationRepository.java");
        Profile requester = requesterResolver.require(requesterId);
        Reservation reservation = reservationRepository.findById(reservaId)
                .orElseThrow(() -> fail(HttpStatus.NOT_FOUND, "Consulta de reserva por id", "Reserva no encontrada"));
        boolean isStaff = requester.getRol() != null && requester.getRol().isStaff();
        if (!isStaff && !Objects.equals(reservation.getPerfilId(), requester.getId())) {
            throw fail(HttpStatus.FORBIDDEN, "Verificación de restricción Cliente vs Staff", "Un cliente solo puede consultar sus propias reservas");
        }
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Consulta de reserva por id\nDe: ReservationServiceImpl.java -> Hacia: ReservationController.java\nEstado: 200 (Éxito - Reserva encontrada)");
        return toResponse(reservation);
    }

    @Override
    public List<ReservationResponse> findAll(Long requesterId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Listado de reservas (Cliente vs Staff)\nDe: ReservationServiceImpl.java -> Hacia: JsonReservationRepository.java");
        Profile requester = requesterResolver.require(requesterId);
        boolean isStaff = requester.getRol() != null && requester.getRol().isStaff();
        List<ReservationResponse> reservas = reservationRepository.findAll().stream()
                .filter(r -> isStaff || Objects.equals(r.getPerfilId(), requester.getId()))
                .sorted(Comparator.comparing(Reservation::getId).reversed())
                .map(ReservationServiceImpl::toResponse)
                .toList();
        System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Listado de reservas (Cliente vs Staff)\nDe: ReservationServiceImpl.java -> Hacia: ReservationController.java\nEstado: 200 (Éxito - " + reservas.size() + " reservas listadas)");
        return reservas;
    }

    private static String generarCodigo() {
        StringBuilder codigo = new StringBuilder(PREFIJO_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo.append(ALFANUMERICO.charAt(ThreadLocalRandom.current().nextInt(ALFANUMERICO.length())));
        }
        return codigo.toString();
    }

    private static ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setCodigo(reservation.getCodigo());
        response.setPerfilId(reservation.getPerfilId());
        response.setPeliculaId(reservation.getPeliculaId());
        response.setPeliculaTitulo(reservation.getPeliculaTitulo());
        response.setSalaNombre(reservation.getSalaNombre());
        response.setHorario(reservation.getHorario());
        response.setAsientos(reservation.getAsientos());
        response.setCantidad(reservation.getCantidad());
        response.setPrecioUnitario(reservation.getPrecioUnitario());
        response.setTotal(reservation.getTotal());
        response.setMetodoPago(reservation.getMetodoPago());
        response.setTarjetaUltimos4(reservation.getTarjetaUltimos4());
        response.setEstado(reservation.getEstado());
        response.setFechaCreacion(reservation.getFechaCreacion());
        return response;
    }

    private static BusinessException fail(HttpStatus status, String traza, String mensaje) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: " + traza
                + "\nDe: ReservationServiceImpl.java\nEstado: " + status.value()
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
