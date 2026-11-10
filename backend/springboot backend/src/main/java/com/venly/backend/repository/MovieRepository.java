package com.venly.backend.repository;

import com.venly.backend.model.Movie;
import java.util.List;
import java.util.Optional;

public interface MovieRepository {

    /** Inserts the movie assigning a new incremental id. */
    Movie save(Movie movie);

    /** Replaces an existing movie (matched by id). */
    Movie update(Movie movie);

    Optional<Movie> findById(Long id);

    List<Movie> findAll();
}
