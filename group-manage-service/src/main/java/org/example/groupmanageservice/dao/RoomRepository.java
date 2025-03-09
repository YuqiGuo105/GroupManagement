package org.example.groupmanageservice.dao;

import org.example.groupmanageservice.modules.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, String> {
}
