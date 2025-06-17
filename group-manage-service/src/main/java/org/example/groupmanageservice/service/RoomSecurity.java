package org.example.groupmanageservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("roomSecurity")
public class RoomSecurity {
    @Autowired
    private RoomService roomService;

    public boolean isHost(String roomId, String hoster) {
        var room = roomService.getRoom(roomId);
        return room != null && room.getHosterUserId().equals(hoster);
    }
}
