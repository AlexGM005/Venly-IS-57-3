package com.venly.backend.service;

import com.venly.backend.dto.SeatMapResponse;

public interface SeatService {

    /** Seat map of the room assigned to the movie; any authenticated role may read it. */
    SeatMapResponse getSeatMap(Long requesterId, Long peliculaId);
}
