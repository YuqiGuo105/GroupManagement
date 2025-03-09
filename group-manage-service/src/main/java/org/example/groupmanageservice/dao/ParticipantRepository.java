package org.example.groupmanageservice.dao;

import org.example.groupmanageservice.modules.Participant;
import org.example.groupmanageservice.modules.domain.ParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, ParticipantId> {
}
