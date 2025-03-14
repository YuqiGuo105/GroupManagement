package org.example.groupmanageservice.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomEventPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private EventType eventType;
    private String roomId;
    private String userId;
}
