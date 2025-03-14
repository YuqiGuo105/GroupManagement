package org.example.groupmanageservice.controller;

import org.example.groupmanageservice.modules.Room;
import org.example.groupmanageservice.service.ParticipantService;
import org.example.groupmanageservice.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = "/test-data.sql")
public class RoomControllerTest {
    @Autowired
    private RoomController roomController;

    // Optionally, if needed for further verifications:
    @Autowired
    private RoomService roomService;

    @Autowired
    private ParticipantService participantService;

    // -----------------------------------------------------------------
    // 1) createRoom(@RequestParam String hoster)
    // -----------------------------------------------------------------
    @Test
    void testCreateRoom_ShouldReturnCreatedRoom() {
        String hoster = "hostUser";
        ResponseEntity<Room> response = roomController.createRoom(hoster);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Room createdRoom = response.getBody();
        assertNotNull(createdRoom);
        assertNotNull(createdRoom.getRoomId());
        assertFalse(createdRoom.getParticipants().isEmpty());
        // Assuming the SQL file or creation logic sets the first participant as the host
        assertEquals(hoster, createdRoom.getParticipants().get(0).getId().getUserId());
    }

    // -----------------------------------------------------------------
    // 2) joinRoom(@RequestParam String roomId,
    //             @RequestParam String password,
    //             @RequestParam String userId)
    // -----------------------------------------------------------------
    @Test
    void testJoinRoom_RoomNotFound_ShouldReturnNotFound() {
        ResponseEntity<String> response = roomController.joinRoom("nonExistingRoomId", "password", "userId");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Room not found", response.getBody());
    }

    @Test
    void testJoinRoom_InvalidPasswordOrNotActive_ShouldReturnForbidden() {
        // In test-data.sql, ensure that room "room1" has joinPassword "111111" and is active.
        ResponseEntity<String> response = roomController.joinRoom("room-1", "wrongPassword", "userId");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid password or room not active", response.getBody());
    }

    @Test
    void testJoinRoom_AlreadyInRoom_ShouldReturnConflict() {
        // In test-data.sql, assume that room "room1" already has a participant with userId "userId".
        ResponseEntity<String> response = roomController.joinRoom("room-1", "111111", "userA");
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already in room", response.getBody());
    }

    @Test
    void testJoinRoom_Success_ShouldReturnOk() {
        // In test-data.sql, ensure that room "room1" is active with joinPassword "password"
        // and that "newUser" is not yet a participant.
        ResponseEntity<String> response = roomController.joinRoom("room-1", "111111", "newUser");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User joined room successfully", response.getBody());
        // Optionally, you can fetch the room to verify that the participant was added.
    }

    // -----------------------------------------------------------------
    // 3) leaveRoom(@RequestParam String roomId,
    //              @RequestParam String userId)
    // -----------------------------------------------------------------
    @Test
    void testLeaveRoom_RoomNotFound_ShouldReturnNotFound() {
        ResponseEntity<String> response = roomController.leaveRoom("unknownRoom", "userId");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Room not found", response.getBody());
    }

    @Test
    void testLeaveRoom_UserNotInRoom_ShouldReturnNotFound() {
        // Assume that in test-data.sql room "room1" does not have a participant with userId "userId".
        ResponseEntity<String> response = roomController.leaveRoom("room-1", "userId");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not in room", response.getBody());
    }

    @Test
    void testLeaveRoom_Success_ShouldReturnOk() {
        // In test-data.sql, assume that room "room1" has a participant "userId"
        // and that after removal at least the host remains.
        ResponseEntity<String> response = roomController.leaveRoom("room-1", "userA");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User left room successfully", response.getBody());
    }

    // -----------------------------------------------------------------
    // 4) closeRoom(@RequestParam String roomId, String hoster)
    // -----------------------------------------------------------------
    @Test
    void testCloseRoom_HosterIsNotOwner_ShouldReturnForbidden() {
        ResponseEntity<String> response = roomController.closeRoom("room-1", "nonHoster");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only the host can close the room", response.getBody());
    }

    @Test
    void testCloseRoom_Success_ShouldReturnOk() {
        ResponseEntity<String> response = roomController.closeRoom("room-1", "host1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Room closed successfully", response.getBody());
    }

    // -----------------------------------------------------------------
    // 5) getRoom(@PathVariable String roomId)
    // -----------------------------------------------------------------
    @Test
    void testGetRoom_RoomFound_ShouldReturnOk() {
        ResponseEntity<Room> response = roomController.getRoom("room-1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Room room = response.getBody();
        assertNotNull(room);
        assertEquals("room-1", room.getRoomId());
    }

    @Test
    void testGetRoom_RoomNotFound_ShouldReturn404() {
        ResponseEntity<Room> response = roomController.getRoom("unknownRoomId");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // -----------------------------------------------------------------
    // 6) updateRoom(@PathVariable String roomId, @RequestBody Room room)
    // -----------------------------------------------------------------
    @Test
    void testUpdateRoom_PathAndBodyMismatch_ShouldReturnBadRequest() {
        Room requestBody = new Room();
        requestBody.setRoomId("bodyRoomId"); // does not match path
        ResponseEntity<Room> response = roomController.updateRoom("pathRoomId", requestBody);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateRoom_Success_ShouldReturnOk() {
        // Assume that in test-data.sql room "room1" exists.
        Room requestBody = new Room();
        requestBody.setRoomId("room-1"); // matches path
        ResponseEntity<Room> response = roomController.updateRoom("room-1", requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Room updatedRoom = response.getBody();
        assertNotNull(updatedRoom);
        assertEquals("room-1", updatedRoom.getRoomId());
    }

    // -----------------------------------------------------------------
    // 7) deleteRoom(@PathVariable String roomId)
    // -----------------------------------------------------------------
    @Test
    void testDeleteRoom_ShouldReturnNoContent() {
        ResponseEntity<Void> response = roomController.deleteRoom("room1");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
}
