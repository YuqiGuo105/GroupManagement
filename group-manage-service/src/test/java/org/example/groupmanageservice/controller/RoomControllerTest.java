package org.example.groupmanageservice.controller;

import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.modules.Room;
import org.example.groupmanageservice.modules.domain.ParticipantId;
import org.example.groupmanageservice.service.ParticipantService;
import org.example.groupmanageservice.service.RoomService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomControllerTest {
    @Mock
    private RoomService roomService;

    @Mock
    private ParticipantService participantService;

    @InjectMocks
    private RoomController roomController;

    private Room activeRoom;
    private Room closedRoom;
    private Participant hostParticipant;

    @BeforeEach
    void setup() {
        // Create a sample active room
        activeRoom = new Room();
        activeRoom.setRoomId("activeRoomId");
        activeRoom.setJoinPassword("123456");
        activeRoom.setStatus(Room.Status.ACTIVE);
        activeRoom.setUpdatedAt(null);

        // Create the host participant and add it to the room's participants list
        hostParticipant = new Participant();
        ParticipantId pid = new ParticipantId("hostUser", "activeRoomId");
        hostParticipant.setId(pid);
        hostParticipant.setPermission(Participant.Permission.HOSTER);
        hostParticipant.setRoom(activeRoom);

        List<Participant> participants = new ArrayList<>();
        participants.add(hostParticipant);
        activeRoom.setParticipants(participants);

        closedRoom = new Room();
        closedRoom.setRoomId("closedRoomId");
        closedRoom.setStatus(Room.Status.CLOSED);
        closedRoom.setParticipants(new ArrayList<>());
    }

    // -----------------------------------------------------------------
    // 1) createRoom(@RequestParam String hoster)
    // -----------------------------------------------------------------
    @Test
    void testCreateRoom_ShouldReturnCreatedRoom() {
        // Arrange
        String hoster = "hostUser";
        when(roomService.createRoom(hoster)).thenReturn(activeRoom);

        // Act
        ResponseEntity<Room> response = roomController.createRoom(hoster);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(activeRoom.getRoomId(), response.getBody().getRoomId());
        // Check that the host is in the participants
        assertEquals(1, response.getBody().getParticipants().size());
        assertEquals("hostUser", response.getBody().getParticipants().get(0).getId().getUserId());
        verify(roomService, times(1)).createRoom(hoster);
        verifyNoMoreInteractions(roomService);
    }

    // -----------------------------------------------------------------
    // 2) joinRoom(@RequestParam String roomId,
    //             @RequestParam String password,
    //             @RequestParam String userId)
    // -----------------------------------------------------------------
    @Test
    void testJoinRoom_RoomNotFound_ShouldReturnNotFound() {
        // Arrange
        when(roomService.getRoomWithParticipants("nonExistingRoomId")).thenReturn(null);

        // Act
        ResponseEntity<String> response = roomController.joinRoom("nonExistingRoomId", "password", "userId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Room not found", response.getBody());
        verifyNoInteractions(participantService);
    }

    @Test
    void testJoinRoom_InvalidPasswordOrNotActive_ShouldReturnForbidden() {
        // Arrange
        // Simulate a room with a different password or not active
        activeRoom.setJoinPassword("111111"); // Different from "password"
        activeRoom.setStatus(Room.Status.ACTIVE);
        when(roomService.getRoomWithParticipants("roomId")).thenReturn(activeRoom);

        // Act
        ResponseEntity<String> response = roomController.joinRoom("roomId", "wrongPassword", "userId");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid password or room not active", response.getBody());
        verifyNoInteractions(participantService);
    }

    @Test
    void testJoinRoom_AlreadyInRoom_ShouldReturnConflict() {
        // Arrange
        Participant existingParticipant = new Participant();
        existingParticipant.setId(new ParticipantId("userId", "roomId"));
        activeRoom.setJoinPassword("password");
        activeRoom.setParticipants(Collections.singletonList(existingParticipant));
        when(roomService.getRoomWithParticipants("roomId")).thenReturn(activeRoom);

        // Act
        ResponseEntity<String> response = roomController.joinRoom("roomId", "password", "userId");

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already in room", response.getBody());
        verifyNoInteractions(participantService);
    }

    @Test
    void testJoinRoom_Success_ShouldReturnOk() {
        // Arrange
        activeRoom.setJoinPassword("password");
        // By default, activeRoom has 1 participant (the host).
        when(roomService.getRoomWithParticipants("roomId")).thenReturn(activeRoom);

        // Act
        ResponseEntity<String> response = roomController.joinRoom("roomId", "password", "newUser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User joined room successfully", response.getBody());
        // Now activeRoom should have 2 participants
        assertEquals(2, activeRoom.getParticipants().size());
        verify(participantService, times(1)).updateParticipant(any(Participant.class));
        verify(roomService, times(1)).updateRoom(any(Room.class));
    }

    // -----------------------------------------------------------------
    // 3) leaveRoom(@RequestParam String roomId,
    //              @RequestParam String userId)
    // -----------------------------------------------------------------
    @Test
    void testLeaveRoom_RoomNotFound_ShouldReturnNotFound() {
        // Arrange
        when(roomService.getRoom("unknownRoom")).thenReturn(null);

        // Act
        ResponseEntity<String> response = roomController.leaveRoom("unknownRoom", "userId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Room not found", response.getBody());
        verifyNoInteractions(participantService);
    }

    @Test
    void testLeaveRoom_UserNotInRoom_ShouldReturnNotFound() {
        // Arrange
        activeRoom.setParticipants(Collections.emptyList());
        when(roomService.getRoom("roomId")).thenReturn(activeRoom);

        // Act
        ResponseEntity<String> response = roomController.leaveRoom("roomId", "userId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not in room", response.getBody());
        verifyNoInteractions(participantService);
    }

    @Test
    void testLeaveRoom_Success_ShouldReturnOk() {
        // Arrange
        Participant participant = new Participant();
        participant.setId(new ParticipantId("userId", "roomId"));
        participant.setPermission(Participant.Permission.PARTICIPANT);

        // activeRoom initially has 1 participant (the host).
        // We add a second participant to simulate a real scenario
        activeRoom.getParticipants().add(participant);
        when(roomService.getRoom("roomId")).thenReturn(activeRoom);

        // Make sure we have 2 participants now
        assertEquals(2, activeRoom.getParticipants().size());

        // Act
        ResponseEntity<String> response = roomController.leaveRoom("roomId", "userId");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User left room successfully", response.getBody());
        // After leaving, there should be exactly 1 participant (the host)
        assertEquals(1, activeRoom.getParticipants().size());
        verify(participantService).deleteParticipant("roomId", "userId");
        verify(roomService).updateRoom(activeRoom);
    }

    // -----------------------------------------------------------------
    // 4) closeRoom(@RequestParam String roomId, String hoster)
    // -----------------------------------------------------------------
    @Test
    void testCloseRoom_HosterIsNotOwner_ShouldReturnForbidden() {
        // Arrange
        doThrow(new IllegalArgumentException("Only the host can close the room"))
                .when(roomService).closeRoom("roomId", "nonHoster");

        // Act
        ResponseEntity<String> response = roomController.closeRoom("roomId", "nonHoster");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only the host can close the room", response.getBody());
    }

    @Test
    void testCloseRoom_Success_ShouldReturnOk() {
        // Arrange
        when(roomService.closeRoom("roomId", "hoster")).thenReturn(closedRoom);

        // Act
        ResponseEntity<String> response = roomController.closeRoom("roomId", "hoster");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Room closed successfully", response.getBody());
        verify(roomService, times(1)).closeRoom("roomId", "hoster");
    }

    // -----------------------------------------------------------------
    // 5) getRoom(@PathVariable String roomId)
    // -----------------------------------------------------------------
    @Test
    void testGetRoom_RoomFound_ShouldReturnOk() {
        // Arrange
        when(roomService.getRoom("activeRoomId")).thenReturn(activeRoom);

        // Act
        ResponseEntity<Room> response = roomController.getRoom("activeRoomId");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("activeRoomId", response.getBody().getRoomId());
        assertEquals(1, response.getBody().getParticipants().size());
        verify(roomService, times(1)).getRoom("activeRoomId");
    }

    @Test
    void testGetRoom_RoomNotFound_ShouldReturn404() {
        // Arrange
        when(roomService.getRoom("unknownRoomId")).thenReturn(null);

        // Act
        ResponseEntity<Room> response = roomController.getRoom("unknownRoomId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(roomService, times(1)).getRoom("unknownRoomId");
    }

    // -----------------------------------------------------------------
    // 6) updateRoom(@PathVariable String roomId, @RequestBody Room room)
    // -----------------------------------------------------------------
    @Test
    void testUpdateRoom_PathAndBodyMismatch_ShouldReturnBadRequest() {
        // Arrange
        Room requestBody = new Room();
        requestBody.setRoomId("bodyRoomId"); // different from path

        // Act
        ResponseEntity<Room> response = roomController.updateRoom("pathRoomId", requestBody);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(roomService);  // We never call the service
    }

    @Test
    void testUpdateRoom_Success_ShouldReturnOk() {
        // Arrange
        Room requestBody = new Room();
        requestBody.setRoomId("activeRoomId"); // matches path
        when(roomService.updateRoom(requestBody)).thenReturn(activeRoom);

        // Act
        ResponseEntity<Room> response = roomController.updateRoom("activeRoomId", requestBody);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("activeRoomId", response.getBody().getRoomId());
        verify(roomService, times(1)).updateRoom(requestBody);
    }

    // -----------------------------------------------------------------
    // 7) deleteRoom(@PathVariable String roomId)
    // -----------------------------------------------------------------
    @Test
    void testDeleteRoom_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(roomService).deleteRoom("activeRoomId");

        // Act
        ResponseEntity<Void> response = roomController.deleteRoom("activeRoomId");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(roomService, times(1)).deleteRoom("activeRoomId");
    }
}
