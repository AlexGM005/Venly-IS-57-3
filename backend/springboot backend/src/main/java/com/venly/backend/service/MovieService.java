package com.venly.backend.service;

import com.venly.backend.dto.CreateMovieRequest;
import com.venly.backend.dto.MovieResponse;
import com.venly.backend.dto.UpdateMovieRequest;
import com.venly.backend.model.Room;
import java.util.List;

public interface MovieService {

    /** Only GERENTE and JEFE may create movies. */
    MovieResponse create(Long requesterId, CreateMovieRequest request);

    /** Only GERENTE and JEFE may edit movies. */
    MovieResponse update(Long requesterId, Long movieId, UpdateMovieRequest request);

    /** Readable by everyone; when a requester id is sent it must belong to an existing profile. */
    List<MovieResponse> findAll(Long requesterId);

    MovieResponse findById(Long requesterId, Long movieId);

    List<Room> findRooms();
}
