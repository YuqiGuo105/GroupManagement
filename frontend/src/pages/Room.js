import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { auth } from '../firebase';
import { getRoom, leaveRoom } from '../api';

export default function RoomPage() {
  const { roomId } = useParams();
  const [room, setRoom] = useState(null);
  const [events, setEvents] = useState([]);
  const user = auth.currentUser;
  const navigate = useNavigate();

  const fetchRoom = useCallback(async () => {
    try {
      const r = await getRoom(roomId);
      setRoom(r);
    } catch (err) {
      console.error(err);
    }
  }, [roomId]);

  useEffect(() => {
    fetchRoom();
    const es = new EventSource(`/events?room=${roomId}`);
    es.onmessage = (e) => {
      const data = JSON.parse(e.data);
      setEvents((prev) => [...prev, data]);
    };
    return () => es.close();
  }, [roomId, fetchRoom]);

  const handleLeave = async () => {
    try {
      await leaveRoom(roomId, user.uid);
      navigate('/');
    } catch (err) {
      alert(err.message);
    }
  };

  if (!room) return <p>Loading...</p>;

  return (
    <div>
      <h2>Room {room.roomId}</h2>
      <p>Host: {room.hosterUserId}</p>
      <button onClick={handleLeave}>Leave Room</button>
      <h3>Participants</h3>
      <ul>
        {room.participants && room.participants.map(p => (
          <li key={p.id.userId}>{p.id.userId} - {p.role}</li>
        ))}
      </ul>
      <h3>Events</h3>
      <ul>
        {events.map((ev, idx) => (
          <li key={idx}>{ev.eventType} - {ev.userId}</li>
        ))}
      </ul>
    </div>
  );
}
