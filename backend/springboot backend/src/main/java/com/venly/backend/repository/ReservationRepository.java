package com.venly.backend.repository;

import com.venly.backend.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    /** Inserts the reservation assigning a new incremental id. */
    Reservation save(Reservation reservation);

    /**
     * Inserts the reservation only when none of its seats is already taken by a confirmed reservation
     * of the same movie; the check and the insert happen atomically.
     */
    Reservation saveIfSeatsAvailable(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByPeliculaId(Long peliculaId);
}
