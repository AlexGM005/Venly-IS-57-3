package com.venly.backend.repository;

import com.venly.backend.model.Room;
import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    Optional<Room> findById(Long id);

    List<Room> findAll();
}
