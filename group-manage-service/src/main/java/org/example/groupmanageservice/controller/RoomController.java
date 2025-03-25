package org.example.groupmanageservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.groupmanageservice.modules.EventType;
import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.modules.Room;
import org.example.groupmanageservice.modules.domain.ParticipantId;
import org.example.groupmanageservice.service.ParticipantService;
import org.example.groupmanageservice.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room API", description = "Operations related to room management")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private ParticipantService participantService;

    // ------------------------------
    // Create Room – generates roomId and joinPassword; adds host as a participant.
    // ------------------------------
    @Operation(summary = "Create Room", description = "Generates a new room with a roomId and joinPassword; adds the host as a participant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room created successfully",
                    content = @Content(schema = @Schema(implementation = Room.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<Room> createRoom(
            @Parameter(description = "User ID of the host creating the room", required = true)
            @RequestParam String hoster) {
        Room room = roomService.createRoom(hoster);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    // ------------------------------
    // Join Room – validates join password and adds the user as a Participant.
    // ------------------------------
    @Operation(summary = "Join Room", description = "Validates join password and adds the user as a participant to the room.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User joined room successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Room not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Invalid password or room not active", content = @Content),
            @ApiResponse(responseCode = "409", description = "User already in room", content = @Content)
    })
    @PostMapping("/join")
    public ResponseEntity<String> joinRoom(
            @Parameter(description = "Room ID", required = true) @RequestParam String roomId,
            @Parameter(description = "Join password for the room", required = true) @RequestParam String password,
            @Parameter(description = "User ID joining the room", required = true) @RequestParam String userId) {
        try {
            String message = roomService.joinRoom(roomId, password, userId);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException ex) {
            if ("Room not found".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            } else if ("Invalid password or room not active".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    // ------------------------------
    // Leave Room – removes the Participant; if host leaves, reassigns host or deletes room if empty.
    // ------------------------------
    @Operation(summary = "Leave Room", description = "Removes the participant from the room; if the host leaves, reassigns host or deletes the room if empty.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User left room successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Room or user not found", content = @Content)
    })
    @PostMapping("/leave")
    public ResponseEntity<String> leaveRoom(
            @Parameter(description = "Room ID", required = true) @RequestParam String roomId,
            @Parameter(description = "User ID leaving the room", required = true) @RequestParam String userId) {
        try {
            String message = roomService.leaveRoom(roomId, userId);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    // ------------------------------
    // Close Room – only the host can close the room.
    // ------------------------------
    @Operation(summary = "Close Room", description = "Closes the room. Only the host can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room closed successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: incorrect host or action not allowed", content = @Content)
    })
    @DeleteMapping("/close")
    public ResponseEntity<String> closeRoom(
            @Parameter(description = "Room ID", required = true) @RequestParam String roomId,
            @Parameter(description = "User ID of the host", required = true) @RequestParam String hoster) {
        try {
            roomService.closeRoom(roomId, hoster);
            return ResponseEntity.ok("Room closed successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    // ------------------------------
    // GET /api/rooms/{roomId} – Retrieve room details.
    // ------------------------------
    @Operation(summary = "Get Room", description = "Retrieve room details by roomId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Room.class))),
            @ApiResponse(responseCode = "404", description = "Room not found", content = @Content)
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable String roomId) {
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    // ------------------------------
    // PUT /api/rooms/{roomId} – Update room details.
    // ------------------------------
    @PutMapping("/{roomId}")
    @Operation(
            summary = "Update Room",
            description = "Update room details. The roomId in the path must match the payload.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Room object containing updated details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Room.class))
            )
    )
    public ResponseEntity<Room> updateRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable String roomId,
            @RequestBody Room room) {
        if (!roomId.equals(room.getRoomId())) {
            return ResponseEntity.badRequest().build();
        }
        Room updated = roomService.updateRoom(room);
        return ResponseEntity.ok(updated);
    }


    // ------------------------------
    // DELETE /api/rooms/{roomId} – Delete a room.
    // ------------------------------
    @Operation(summary = "Delete Room", description = "Delete a room by roomId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Room not found", content = @Content)
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------
    // Get All Rooms – new endpoint to retrieve all rooms.
    // ------------------------------
    @Operation(summary = "Get All Rooms", description = "Retrieve all rooms.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All rooms retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Room.class)))
    })
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
}
