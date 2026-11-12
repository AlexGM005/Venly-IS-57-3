package com.venly.backend.service.impl;

import com.venly.backend.dto.CreateMovieRequest;
import com.venly.backend.dto.MovieResponse;
import com.venly.backend.dto.UpdateMovieRequest;
import com.venly.backend.exception.BusinessException;
import com.venly.backend.model.Movie;
import com.venly.backend.model.Room;
import com.venly.backend.repository.MovieRepository;
import com.venly.backend.repository.RoomRepository;
import com.venly.backend.service.MovieService;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MovieServiceImpl implements MovieService {

    static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final String JPEG = "image/jpeg";
    private static final String PNG = "image/png";
    private static final String FORBIDDEN_MESSAGE = "Solo un gerente o jefe puede gestionar películas";

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final RequesterResolver requesterResolver;

    public MovieServiceImpl(MovieRepository movieRepository,
                            RoomRepository roomRepository,
                            RequesterResolver requesterResolver) {
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.requesterResolver = requesterResolver;
    }

    @Override
    public MovieResponse create(Long requesterId, CreateMovieRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Verificando restricción de Staff (Gerente/Jefe) y creando película en backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonMovieRepository.java");
        requesterResolver.requireStaff(requesterId, FORBIDDEN_MESSAGE);
        Movie movie = new Movie();
        applyFields(movie, request);
        if (hasText(request.getImagenBase64())) {
            applyImage(movie, request.getImagenBase64(), request.getImagenTipo());
        }
        return toResponse(movieRepository.save(movie), roomNames());
    }

    @Override
    public MovieResponse update(Long requesterId, Long movieId, UpdateMovieRequest request) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Verificando restricción de Staff (Gerente/Jefe) y editando película en backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonMovieRepository.java");
        requesterResolver.requireStaff(requesterId, FORBIDDEN_MESSAGE);
        Movie movie = findMovie(movieId);
        applyFields(movie, request);
        if (hasText(request.getImagenBase64())) {
            applyImage(movie, request.getImagenBase64(), request.getImagenTipo());
        } else if (request.isQuitarImagen()) {
            movie.setImagenBase64(null);
            movie.setImagenTipo(null);
        }
        return toResponse(movieRepository.update(movie), roomNames());
    }

    @Override
    public List<MovieResponse> findAll(Long requesterId) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Consultando cartelera completa de películas en backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonMovieRepository.java");
        validateOptionalRequester(requesterId);
        Map<Long, String> rooms = roomNames();
        return movieRepository.findAll().stream()
                .sorted(Comparator.comparing(Movie::getId))
                .map(movie -> toResponse(movie, rooms))
                .toList();
    }

    @Override
    public MovieResponse findById(Long requesterId, Long movieId) {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Consultando película por id en backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonMovieRepository.java");
        validateOptionalRequester(requesterId);
        return toResponse(findMovie(movieId), roomNames());
    }

    @Override
    public List<Room> findRooms() {
        System.out.println("[VENLY TRACE - BACKEND] Acción: Consultando listado de salas disponibles en backend/springboot backend/src/main/java/com/venly/backend/service/impl/MovieServiceImpl.java -> consultando repositorio backend/springboot backend/src/main/java/com/venly/backend/repository/JsonRoomRepository.java");
        return roomRepository.findAll().stream().sorted(Comparator.comparing(Room::getId)).toList();
    }

    private void validateOptionalRequester(Long requesterId) {
        if (requesterId != null) {
            requesterResolver.require(requesterId);
        }
    }

    private Movie findMovie(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Película no encontrada"));
    }

    private void applyFields(Movie movie, CreateMovieRequest request) {
        if (roomRepository.findById(request.getSalaId()).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La sala seleccionada no existe");
        }
        String[] times = request.getHorario().split("-");
        String start = times[0].trim();
        String end = times[1].trim();
        if (start.equals(end)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La hora de inicio y fin no pueden ser iguales");
        }
        movie.setTitulo(request.getTitulo().trim());
        movie.setHorario(start + " - " + end);
        movie.setDuracion(request.getDuracion().trim());
        movie.setSinopsis(request.getSinopsis().trim());
        movie.setClasificacion(request.getClasificacion().trim());
        movie.setSalaId(request.getSalaId());
    }

    /** Validates type (JPG/PNG, checked against the file signature) and size (max 10 MB). */
    private void applyImage(Movie movie, String rawBase64, String declaredType) {
        String base64 = rawBase64.trim();
        String type = declaredType;
        if (base64.startsWith("data:")) {
            int comma = base64.indexOf(',');
            if (comma < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "La imagen no tiene un formato base64 válido");
            }
            if (!hasText(type)) {
                type = base64.substring(5, base64.indexOf(';') > 0 ? base64.indexOf(';') : comma);
            }
            base64 = base64.substring(comma + 1);
        }
        type = normalizeType(type);

        byte[] bytes;
        try {
            bytes = Base64.getMimeDecoder().decode(base64);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La imagen no tiene un formato base64 válido");
        }
        if (bytes.length == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La imagen está vacía");
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La imagen no puede superar 10 MB");
        }
        if (!matchesSignature(bytes, type)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El contenido de la imagen no corresponde a un JPG o PNG válido");
        }
        movie.setImagenBase64(Base64.getEncoder().encodeToString(bytes));
        movie.setImagenTipo(type);
    }

    private static String normalizeType(String type) {
        String value = type == null ? "" : type.trim().toLowerCase();
        if (value.equals("image/jpg")) {
            value = JPEG;
        }
        if (!value.equals(JPEG) && !value.equals(PNG)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La imagen debe ser JPG o PNG");
        }
        return value;
    }

    private static boolean matchesSignature(byte[] bytes, String type) {
        if (JPEG.equals(type)) {
            return bytes.length > 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        }
        return bytes.length > 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
    }

    private Map<Long, String> roomNames() {
        return roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getId, Room::getNombre, (a, b) -> a));
    }

    private static MovieResponse toResponse(Movie movie, Map<Long, String> roomNames) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitulo(movie.getTitulo());
        response.setHorario(movie.getHorario());
        response.setDuracion(movie.getDuracion());
        response.setSinopsis(movie.getSinopsis());
        response.setClasificacion(movie.getClasificacion());
        response.setImagenBase64(movie.getImagenBase64());
        response.setImagenTipo(movie.getImagenTipo());
        response.setSalaId(movie.getSalaId());
        response.setSalaNombre(roomNames.getOrDefault(movie.getSalaId(), "Sala no disponible"));
        return response;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
