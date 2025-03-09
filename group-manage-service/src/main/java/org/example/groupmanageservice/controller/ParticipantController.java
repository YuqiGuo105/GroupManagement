package org.example.groupmanageservice.controller;

import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {
    @Autowired
    private ParticipantService participantService;

    // GET /api/participants?roomId={roomId}&userId={userId} – Retrieve a participant.
    @GetMapping
    public ResponseEntity<Participant> getParticipant(@RequestParam String roomId, @RequestParam String userId) {
        Participant participant = participantService.getParticipant(roomId, userId);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(participant);
    }

    // PUT /api/participants – Update participant details.
    @PutMapping
    public ResponseEntity<Participant> updateParticipant(@RequestBody Participant participant) {
        Participant updated = participantService.updateParticipant(participant);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/participants?roomId={roomId}&userId={userId} – Delete a participant.
    @DeleteMapping
    public ResponseEntity<Void> deleteParticipant(@RequestParam String roomId, @RequestParam String userId) {
        participantService.deleteParticipant(roomId, userId);
        return ResponseEntity.noContent().build();
    }
}
