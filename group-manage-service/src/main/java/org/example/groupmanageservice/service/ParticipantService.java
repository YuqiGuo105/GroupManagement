package org.example.groupmanageservice.service;

import org.springframework.cache.annotation.Cacheable;
import org.example.groupmanageservice.dao.ParticipantRepository;
import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.modules.domain.ParticipantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParticipantService {
    @Autowired
    private ParticipantRepository participantRepository;

    @Cacheable(value = "participants", key = "'' + #roomId + ':' + #userId")
    public Participant getParticipant(String roomId, String userId) {
        ParticipantId id = new ParticipantId(userId, roomId);
        Optional<Participant> participant = participantRepository.findById(id);
        return participant.orElse(null);
    }

    @CachePut(value = "participants", key = "'' + #participant.id.roomId + ':' + #participant.id.userId")
    public Participant updateParticipant(Participant participant) {
        return participantRepository.save(participant);
    }

    @CacheEvict(value = "participants", key = "'' + #roomId + ':' + #userId")
    public void deleteParticipant(String roomId, String userId) {
        ParticipantId id = new ParticipantId(userId, roomId);
        participantRepository.deleteById(id);
    }
}
