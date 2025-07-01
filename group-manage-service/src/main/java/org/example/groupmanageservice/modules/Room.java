package org.example.groupmanageservice.modules;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rooms")
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String roomId;

    private String hosterUserId;
    private String joinPassword;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Use plain DATETIME to stay compatible with older MySQL versions
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    // Use lazy fetching (or eager if small), but use JsonManagedReference to break recursion.
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Participant> participants;

    public enum Status {
        ACTIVE, CLOSED
    }
}
