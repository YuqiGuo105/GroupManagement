package org.example.groupmanageservice.controller;

import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.modules.Room;
import org.example.groupmanageservice.modules.domain.ParticipantId;
import org.example.groupmanageservice.service.ParticipantService;
import org.example.groupmanageservice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private ParticipantService participantService;

    // ------------------------------
    // Create Room – generates roomId and joinPassword; adds host as a participant.
    // ------------------------------
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(@RequestParam String hoster) {
        Room room = roomService.createRoom(hoster);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    // ------------------------------
    // Join Room – validates join password and adds the user as a Participant.
    // ------------------------------
    @PostMapping("/join")
    public ResponseEntity<String> joinRoom(@RequestParam String roomId,
                                           @RequestParam String password,
                                           @RequestParam String userId) {
        // Use the transactional method to load the room with participants initialized.
        Room room = roomService.getRoomWithParticipants(roomId);
        if (room == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }
        if (!room.getJoinPassword().equals(password) || room.getStatus() != Room.Status.ACTIVE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid password or room not active");
        }
        boolean alreadyIn = room.getParticipants().stream()
                .anyMatch(p -> p.getId().getUserId().equals(userId));
        if (alreadyIn) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already in room");
        }
        // Create a new Participant (this method will insert it into the DB)
        Participant newParticipant = new Participant();
        newParticipant.setId(new ParticipantId(userId, roomId));
        newParticipant.setPermission(Participant.Permission.PARTICIPANT);
        newParticipant.setRoom(room);
        participantService.updateParticipant(newParticipant);
        // Add participant to the room and update the room in DB/cache
        room.getParticipants().add(newParticipant);
        roomService.updateRoom(room);
        return ResponseEntity.ok("User joined room successfully");
    }

    // ------------------------------
    // Leave Room – removes the Participant; if host leaves, reassigns host or deletes room if empty.
    // ------------------------------
    @PostMapping("/leave")
    public ResponseEntity<String> leaveRoom(@RequestParam String roomId,
                                            @RequestParam String userId) {
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }
        // Find the participant in the room
        Optional<Participant> participantOpt = room.getParticipants().stream()
                .filter(p -> p.getId().getUserId().equals(userId))
                .findFirst();
        if (!participantOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not in room");
        }
        Participant participant = participantOpt.get();
        // Remove participant from room and delete from DB/cache
        room.getParticipants().remove(participant);
        participantService.deleteParticipant(roomId, userId);
        // If the leaving participant is the host, reassign host or delete room if no one remains
        if (participant.getPermission() == Participant.Permission.HOSTER) {
            if (!room.getParticipants().isEmpty()) {
                Participant newHost = room.getParticipants().get(0);
                newHost.setPermission(Participant.Permission.HOSTER);
                room.setHosterUserId(newHost.getId().getUserId());
                participantService.updateParticipant(newHost);
            } else {
                room.setStatus(Room.Status.CLOSED);
                room.getParticipants().clear();
                roomService.updateRoom(room);
                return ResponseEntity.ok("Room deleted as it is empty");
            }
        }
        roomService.updateRoom(room);
        return ResponseEntity.ok("User left room successfully");
    }

    // ------------------------------
    // Close Room – only the host can close the room.
    // ------------------------------
    @DeleteMapping("/close")
    public ResponseEntity<String> closeRoom(@RequestParam String roomId,
                                            @RequestParam String hoster) {
        try {
            Room updatedRoom = roomService.closeRoom(roomId, hoster);
            return ResponseEntity.ok("Room closed successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    // GET /api/rooms/{roomId} – Retrieve room details.
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    // PUT /api/rooms/{roomId} – Update room details.
    // (Make sure roomId in path matches the payload)
    @PutMapping("/{roomId}")
    public ResponseEntity<Room> updateRoom(@PathVariable String roomId, @RequestBody Room room) {
        if (!roomId.equals(room.getRoomId())) {
            return ResponseEntity.badRequest().build();
        }
        Room updated = roomService.updateRoom(room);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/rooms/{roomId} – Delete a room.
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
