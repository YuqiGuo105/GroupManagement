package org.example.groupmanageservice.service;

import org.example.groupmanageservice.dao.RoomRepository;
import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.modules.Room;
import org.example.groupmanageservice.modules.domain.ParticipantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Sql(scripts = "/test-data.sql")
public class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    /**
     * Function: Test that getRoom returns an existing room.
     * Edge case: When a room with the given roomId does not exist.
     */
    @Test
    @Transactional
    void testGetRoom_WithExistingRoom() {
        Room room = roomService.getRoom("room-1");
        assertNotNull(room, "Room should not be null");
        assertEquals("room-1", room.getRoomId(), "Room ID must match");
        assertEquals("111111", room.getJoinPassword(), "Join password must match");
    }

    /**
     * Function: Test that getRoomWithParticipants returns a room with its participants initialized.
     * Edge case: The participants list should not be null and must contain the dummy data.
     */
    @Test
    @Transactional
    void testGetRoomWithParticipants_ShouldInitializeParticipants() {
        Room room = roomService.getRoomWithParticipants("room-1");
        assertNotNull(room, "Room should not be null");
        assertNotNull(room.getParticipants(), "Participants list should be initialized");
        // In test-data.sql, room-1 has 3 participants.
        assertEquals(3, room.getParticipants().size(), "There should be 3 participants");
    }

    /**
     * Function: Test that closeRoom successfully closes an active room.
     * Edge case: The room's status changes to CLOSED and its participants list is cleared.
     */
    @Test
    @Transactional
    void testCloseRoom_Success() {
        Room closed = roomService.closeRoom("room-1", "host1");
        assertNotNull(closed, "Closed room should not be null");
        assertEquals(Room.Status.CLOSED, closed.getStatus(), "Room status should be CLOSED");
        assertNotNull(closed.getParticipants(), "Participants list should be initialized");
        assertEquals(0, closed.getParticipants().size(), "Participants list should be empty after closing");
    }

    /**
     * Function: Test that closing a room with an invalid host throws an exception.
     * Edge case: Only the host should be able to close the room.
     */
    @Test
    void testCloseRoom_InvalidHost_ShouldThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            roomService.closeRoom("room-1", "notHost");
        });
        assertEquals("Only the host can close the room", ex.getMessage());
    }

    /**
     * Function: Test that createRoom returns a properly created room with a generated roomId, joinPassword,
     * ACTIVE status, and a host participant correctly added.
     * Edge case: The returned room must have a non-null roomId and joinPassword; its participants list should
     * contain exactly one participant with the correct host userId.
     */
    @Test
    @Transactional
    void testCreateRoom_ShouldReturnCreatedRoom() {
        // Act: Create a new room using a new host
        Room newRoom = roomService.createRoom("newHost");
        // Assert: newRoom is properly constructed
        assertNotNull(newRoom, "Created room should not be null");
        assertNotNull(newRoom.getRoomId(), "Room ID should be generated");
        assertNotNull(newRoom.getJoinPassword(), "Join password should be generated");
        assertEquals(Room.Status.ACTIVE, newRoom.getStatus(), "New room should be ACTIVE");
        assertNotNull(newRoom.getParticipants(), "Participants list should be initialized");
        assertEquals(1, newRoom.getParticipants().size(), "There should be exactly one participant (the host)");
        assertEquals("newHost", newRoom.getParticipants().get(0).getId().getUserId(), "Host userId must match");
    }
}
