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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room mockRoom;
    private Participant hostParticipant;

    @BeforeEach
    void setup() {
        mockRoom = new Room();
        mockRoom.setRoomId("testRoomId");
        mockRoom.setHosterUserId("hostUser");
        mockRoom.setStatus(Room.Status.ACTIVE);

        // Create the host participant and add it to the room's participants list
        hostParticipant = new Participant();
        ParticipantId pid = new ParticipantId("hostUser", "activeRoomId");
        hostParticipant.setId(pid);
        hostParticipant.setPermission(Participant.Permission.HOSTER);
        hostParticipant.setRoom(mockRoom);

        List<Participant> participants = new ArrayList<>();
        participants.add(hostParticipant);
        mockRoom.setParticipants(participants);
    }

    // -----------------------------------------------------------------
    // createRoom(String hosterUserId)
    // -----------------------------------------------------------------
    @Test
    void testCreateRoom_ShouldCreateAndReturnRoom() {
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);

        Room result = roomService.createRoom("hostUser");

        assertNotNull(result);
        assertEquals("testRoomId", result.getRoomId());
        assertEquals("hostUser", result.getHosterUserId());
        assertEquals(Room.Status.ACTIVE, result.getStatus());

        // Verify the new room has exactly one participant: the host
        assertNotNull(result.getParticipants());
        assertEquals(1, result.getParticipants().size(),
                "Expected exactly one participant (the host) in the created room");

        verify(roomRepository, times(1)).save(any(Room.class));
    }

    // -----------------------------------------------------------------
    // closeRoom(String roomId, String hoster)
    // -----------------------------------------------------------------
    @Test
    void testCloseRoom_NotFound_ShouldThrow() {
        when(roomRepository.findById("nonExisting")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                roomService.closeRoom("nonExisting", "anyHost")
        );
    }

    @Test
    void testCloseRoom_HosterDoesNotMatch_ShouldThrow() {
        when(roomRepository.findById("testRoomId")).thenReturn(Optional.of(mockRoom));

        assertThrows(IllegalArgumentException.class, () ->
                roomService.closeRoom("testRoomId", "differentHost")
        );
    }

    @Test
    void testCloseRoom_Success_ShouldReturnClosedRoom() {
        // Suppose we have the host in the room. The service will clear participants on close.
        when(roomRepository.findById("testRoomId")).thenReturn(Optional.of(mockRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.closeRoom("testRoomId", "hostUser");

        assertNotNull(result);
        assertEquals(Room.Status.CLOSED, result.getStatus());

        // Since closeRoom() calls room.getParticipants().clear(), check that participants are removed
        assertNotNull(result.getParticipants());
        assertEquals(0, result.getParticipants().size(),
                "Expected participants to be cleared after closing the room");

        verify(roomRepository, times(1)).save(any(Room.class));
    }

    // -----------------------------------------------------------------
    // getRoom(String roomId)
    // -----------------------------------------------------------------
    @Test
    void testGetRoom_Found_ShouldReturnRoom() {
        when(roomRepository.findById("testRoomId")).thenReturn(Optional.of(mockRoom));

        Room result = roomService.getRoom("testRoomId");

        assertNotNull(result);
        assertEquals("testRoomId", result.getRoomId());
    }

    @Test
    void testGetRoom_NotFound_ShouldReturnNull() {
        when(roomRepository.findById("unknown")).thenReturn(Optional.empty());

        Room result = roomService.getRoom("unknown");

        assertNull(result);
    }

    // -----------------------------------------------------------------
    // updateRoom(Room room)
    // -----------------------------------------------------------------
    @Test
    void testUpdateRoom_ShouldSaveUpdatedRoom() {
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Room roomToUpdate = new Room();
        roomToUpdate.setRoomId("testRoomId");

        Room result = roomService.updateRoom(roomToUpdate);

        assertNotNull(result);
        assertEquals("testRoomId", result.getRoomId());
        verify(roomRepository, times(1)).save(roomToUpdate);
    }
}
