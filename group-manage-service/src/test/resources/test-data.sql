-- Clear existing data
DELETE FROM participants;
DELETE FROM rooms;

-- Insert dummy rooms
INSERT INTO rooms (room_id, hoster_user_id, join_password, status, created_at, updated_at)
VALUES ('room-1', 'host1', '111111', 'ACTIVE', CURRENT_TIMESTAMP, NULL);
INSERT INTO rooms (room_id, hoster_user_id, join_password, status, created_at, updated_at)
VALUES ('room-2', 'host2', '222222', 'ACTIVE', CURRENT_TIMESTAMP, NULL);
INSERT INTO rooms (room_id, hoster_user_id, join_password, status, created_at, updated_at)
VALUES ('room-3', 'host3', '333333', 'CLOSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert dummy participants for room-1 (one per statement)
INSERT INTO participants (user_id, room_id, role, permission)
VALUES ('host1', 'room-1', 'HOSTER', 'READ_WRITE');
INSERT INTO participants (user_id, room_id, role, permission)
VALUES ('userA', 'room-1', 'PARTICIPANT', 'READ');
INSERT INTO participants (user_id, room_id, role, permission)
VALUES ('userB', 'room-1', 'PARTICIPANT', 'READ');

-- Insert dummy participants for room-2
INSERT INTO participants (user_id, room_id, role, permission)
VALUES ('host2', 'room-2', 'HOSTER', 'READ_WRITE');
INSERT INTO participants (user_id, room_id, role, permission)
VALUES ('userC', 'room-2', 'PARTICIPANT', 'READ');
