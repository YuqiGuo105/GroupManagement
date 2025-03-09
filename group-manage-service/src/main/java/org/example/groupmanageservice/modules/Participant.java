package org.example.groupmanageservice.modules;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.groupmanageservice.modules.domain.ParticipantId;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "participants")
public class Participant implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "user_id")),
            @AttributeOverride(name = "roomId", column = @Column(name = "room_id", insertable = false, updatable = false))
    })
    private ParticipantId id;

    @Enumerated(EnumType.STRING)
    private Permission permission;

    // Remove any redundant field like "private String roomId;"—the id already contains roomId.

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    @JsonBackReference
    private Room room;

    public enum Permission {
        HOSTER, PARTICIPANT
    }
}
