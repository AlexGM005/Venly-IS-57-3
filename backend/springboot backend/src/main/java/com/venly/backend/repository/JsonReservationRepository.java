package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Reservation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

/** Reservation persistence backed by the "Reservas" collection of the JSON data store. */
@Repository
public class JsonReservationRepository implements ReservationRepository {

    private static final String COLLECTION_KEY = "Reservas";
    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";
    private static final TypeReference<List<Reservation>> RESERVATION_LIST_TYPE = new TypeReference<>() {
    };

    private final JsonDataStore dataStore;

    public JsonReservationRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Reservation save(Reservation reservation) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Inserción de nueva reserva\nDe: JsonReservationRepository.java -> Hacia: data/Base.json");
        return dataStore.insert(COLLECTION_KEY, RESERVATION_LIST_TYPE, reservation, Reservation::getId, Reservation::setId);
    }

    @Override
    public Reservation saveIfSeatsAvailable(Reservation reservation) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Verificación atómica de disponibilidad de asientos\nDe: JsonReservationRepository.java -> Hacia: data/Base.json");
        return dataStore.updateCollection(COLLECTION_KEY, RESERVATION_LIST_TYPE, reservations -> {
            List<String> conflictos = reservations.stream()
                    .filter(r -> ESTADO_CONFIRMADA.equals(r.getEstado()))
                    .filter(r -> Objects.equals(r.getPeliculaId(), reservation.getPeliculaId()))
                    .filter(r -> r.getAsientos() != null)
                    .flatMap(r -> r.getAsientos().stream())
                    .filter(asiento -> reservation.getAsientos().contains(asiento))
                    .distinct()
                    .sorted()
                    .toList();
            if (!conflictos.isEmpty()) {
                System.out.println("\n[VENLY TRACE - BACKEND] 🔴 Acción: Verificación atómica de disponibilidad de asientos\nDe: JsonReservationRepository.java\nEstado: 409 (Conflicto - Los asientos " + String.join(", ", conflictos) + " ya no están disponibles)");
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Los asientos " + String.join(", ", conflictos) + " ya no están disponibles");
            }
            long nextId = reservations.stream()
                    .map(Reservation::getId)
                    .filter(Objects::nonNull)
                    .max(Long::compareTo)
                    .orElse(0L) + 1;
            reservation.setId(nextId);
            reservations.add(reservation);
            System.out.println("\n[VENLY TRACE - BACKEND] 🟢 Acción: Verificación atómica de disponibilidad de asientos\nDe: JsonReservationRepository.java -> Hacia: data/Base.json\nEstado: 201 (Creado - Reserva " + reservation.getCodigo() + " guardada)");
            return reservation;
        });
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de reserva por id\nDe: JsonReservationRepository.java -> Hacia: data/Base.json");
        if (id == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(r -> Objects.equals(r.getId(), id)).findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Listado completo de reservas\nDe: JsonReservationRepository.java -> Hacia: data/Base.json");
        return dataStore.getCollection(COLLECTION_KEY, RESERVATION_LIST_TYPE);
    }

    @Override
    public List<Reservation> findByPeliculaId(Long peliculaId) {
        System.out.println("\n[VENLY TRACE - BACKEND] 🔵 Acción: Consulta de reservas por función\nDe: JsonReservationRepository.java -> Hacia: data/Base.json");
        if (peliculaId == null) {
            return List.of();
        }
        return findAll().stream().filter(r -> Objects.equals(r.getPeliculaId(), peliculaId)).toList();
    }
}
