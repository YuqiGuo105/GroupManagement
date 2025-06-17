package org.example.groupmanageservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.service.ParticipantService;
import org.example.groupmanageservice.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/participants")
@Tag(name = "Participant API", description = "Operations related to participant management")
public class ParticipantController {
    @Autowired
    private ParticipantService participantService;

    @Autowired
    private RoomService roomService;

    // GET /api/participants?roomId={roomId}&userId={userId} – Retrieve a participant.
    @Operation(summary = "Retrieve Participant", description = "Get a participant by roomId and userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Participant.class))),
            @ApiResponse(responseCode = "404", description = "Participant not found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Participant> getParticipant(
            @Parameter(description = "Room ID", required = true) @RequestParam String roomId,
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        Participant participant = participantService.getParticipant(roomId, userId);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(participant);
    }

    // PUT /api/participants – Update participant details.
    @Operation(
            summary = "Update Participant",
            description = "Update participant details",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Participant object containing updated details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Participant.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant updated successfully",
                    content = @Content(schema = @Schema(implementation = Participant.class))),
            @ApiResponse(responseCode = "400", description = "Invalid participant data", content = @Content)
    })
    @PutMapping
    public ResponseEntity<Participant> updateParticipant(@RequestBody Participant participant) {
        Participant updated = participantService.updateParticipant(participant);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/participants?roomId={roomId}&userId={userId} – Delete a participant.
    @Operation(summary = "Delete Participant", description = "Delete a participant by roomId and userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Participant deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Participant not found", content = @Content)
    })
    @DeleteMapping
    @PreAuthorize("@roomSecurity.isHost(#roomId, #hoster)")
    public ResponseEntity<Void> deleteParticipant(
            @Parameter(description = "Room ID", required = true) @RequestParam String roomId,
            @Parameter(description = "User ID to remove", required = true) @RequestParam String userId,
            @Parameter(description = "Host user ID performing the action", required = true)
            @RequestParam String hoster) {
        roomService.removeParticipant(roomId, hoster, userId);
        return ResponseEntity.noContent().build();
    }
}
