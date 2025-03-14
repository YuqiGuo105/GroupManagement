package org.example.groupmanageservice.modules;


import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class RoomEvent extends ApplicationEvent {
    private final EventType eventType;
    private final String roomId;
    private final String userId;

    public RoomEvent(Object source, EventType eventType, String roomId, String userId) {
        super(source);
        this.eventType = eventType;
        this.roomId = roomId;
        this.userId = userId;
    }

}
