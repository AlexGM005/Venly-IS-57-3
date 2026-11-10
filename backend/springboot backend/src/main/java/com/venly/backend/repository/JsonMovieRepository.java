package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.venly.backend.model.Movie;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Movie persistence backed by the "Peliculas" collection of the JSON data store. */
@Repository
public class JsonMovieRepository implements MovieRepository {

    private static final String COLLECTION_KEY = "Peliculas";
    private static final TypeReference<List<Movie>> MOVIE_LIST_TYPE = new TypeReference<>() {
    };

    private final JsonDataStore dataStore;

    public JsonMovieRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Movie save(Movie movie) {
        return dataStore.insert(COLLECTION_KEY, MOVIE_LIST_TYPE, movie, Movie::getId, Movie::setId);
    }

    @Override
    public Movie update(Movie movie) {
        return dataStore.replace(COLLECTION_KEY, MOVIE_LIST_TYPE, movie, Movie::getId);
    }

    @Override
    public Optional<Movie> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(m -> Objects.equals(m.getId(), id)).findFirst();
    }

    @Override
    public List<Movie> findAll() {
        return dataStore.getCollection(COLLECTION_KEY, MOVIE_LIST_TYPE);
    }
}
