package org.example.groupmanageservice.modules.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ParticipantId implements Serializable {
    @Column(name = "user_id")
    private String userId;

    // Mark roomId as read-only in the embeddable so that the @ManyToOne mapping manages it.
    @Column(name = "room_id", insertable = false, updatable = false)
    private String roomId;
}
