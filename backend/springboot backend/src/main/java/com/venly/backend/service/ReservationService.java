package com.venly.backend.service;

import com.venly.backend.dto.CreateReservationRequest;
import com.venly.backend.dto.ReservationResponse;
import java.util.List;

public interface ReservationService {

    /** Only CLIENTE may reserve; the simulated payment is validated before persisting. */
    ReservationResponse create(Long requesterId, CreateReservationRequest request);

    /** CLIENTE only sees its own reservations; GERENTE and JEFE see any of them. */
    ReservationResponse findById(Long requesterId, Long reservaId);

    /** CLIENTE gets only its own reservations, staff gets all of them (newest first). */
    List<ReservationResponse> findAll(Long requesterId);
}
