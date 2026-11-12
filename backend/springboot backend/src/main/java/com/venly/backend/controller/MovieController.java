package com.venly.backend.controller;

import com.venly.backend.dto.CreateMovieRequest;
import com.venly.backend.dto.MovieResponse;
import com.venly.backend.dto.UpdateMovieRequest;
import com.venly.backend.model.Room;
import com.venly.backend.service.MovieService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/peliculas")
public class MovieController {

    private static final String SESSION_HEADER = ProfileController.SESSION_HEADER;

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<MovieResponse> create(@RequestHeader(SESSION_HEADER) Long requesterId,
                                                @Valid @RequestBody CreateMovieRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición POST /api/peliculas (creación exclusiva Gerente/Jefe) en backend/springboot backend/src/main/java/com/venly/backend/controller/MovieController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java");
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(requesterId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> update(@RequestHeader(SESSION_HEADER) Long requesterId,
                                                @PathVariable Long id,
                                                @Valid @RequestBody UpdateMovieRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición PUT /api/peliculas/{id} (edición exclusiva Gerente/Jefe) en backend/springboot backend/src/main/java/com/venly/backend/controller/MovieController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java");
        return ResponseEntity.ok(movieService.update(requesterId, id, request));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> findAll(
            @RequestHeader(value = SESSION_HEADER, required = false) Long requesterId) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición GET /api/peliculas (consulta de cartelera) en backend/springboot backend/src/main/java/com/venly/backend/controller/MovieController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java");
        return ResponseEntity.ok(movieService.findAll(requesterId));
    }

    /** Rooms available for the movie form selector. */
    @GetMapping("/salas")
    public ResponseEntity<List<Room>> findRooms() {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición GET /api/peliculas/salas en backend/springboot backend/src/main/java/com/venly/backend/controller/MovieController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java");
        return ResponseEntity.ok(movieService.findRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> findById(
            @RequestHeader(value = SESSION_HEADER, required = false) Long requesterId,
            @PathVariable Long id) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Recibiendo petición GET /api/peliculas/{id} en backend/springboot backend/src/main/java/com/venly/backend/controller/MovieController.java -> delegando a backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java");
        return ResponseEntity.ok(movieService.findById(requesterId, id));
    }
}
