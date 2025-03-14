package org.example.groupmanageservice.dao;

import org.example.groupmanageservice.modules.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.participants WHERE r.roomId = :roomId")
    Optional<Room> findByIdWithParticipants(@Param("roomId") String roomId);
}
