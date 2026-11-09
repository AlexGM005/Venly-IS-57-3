package com.venly.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.venly.backend.model.Room;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.LongStream;
import org.springframework.stereotype.Repository;

/** Room persistence backed by the "Salas" collection; seeds the 5 default rooms when the collection is empty. */
@Repository
public class JsonRoomRepository implements RoomRepository {

    private static final String COLLECTION_KEY = "Salas";
    private static final int DEFAULT_ROOMS = 5;
    private static final int DEFAULT_CAPACITY = 120;
    private static final TypeReference<List<Room>> ROOM_LIST_TYPE = new TypeReference<>() {
    };

    private final JsonDataStore dataStore;

    public JsonRoomRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @PostConstruct
    void seedDefaultRooms() {
        dataStore.updateCollection(COLLECTION_KEY, ROOM_LIST_TYPE, rooms -> {
            if (rooms.isEmpty()) {
                LongStream.rangeClosed(1, DEFAULT_ROOMS)
                        .forEach(i -> rooms.add(new Room(i, "Sala " + i, DEFAULT_CAPACITY)));
            }
            return null;
        });
    }

    @Override
    public Optional<Room> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(r -> Objects.equals(r.getId(), id)).findFirst();
    }

    @Override
    public List<Room> findAll() {
        return dataStore.getCollection(COLLECTION_KEY, ROOM_LIST_TYPE);
    }
}
