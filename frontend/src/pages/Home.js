import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase';
import { createRoom, joinRoom, leaveRoom, getAllRooms } from '../api';
import { signOut } from 'firebase/auth';

export default function Home() {
  const [rooms, setRooms] = useState([]);
  const [joinRoomId, setJoinRoomId] = useState('');
  const [joinPassword, setJoinPassword] = useState('');
  const navigate = useNavigate();

  const user = auth.currentUser;

  useEffect(() => { if (user) fetchRooms(); }, [user]);

  const fetchRooms = async () => {
    try {
      const data = await getAllRooms();
      setRooms(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCreate = async () => {
    try {
      const room = await createRoom(user.uid);
      navigate(`/room/${room.roomId}`);
    } catch (err) {
      alert(err.message);
    }
  };

  const handleJoin = async () => {
    try {
      await joinRoom(joinRoomId, joinPassword, user.uid);
      navigate(`/room/${joinRoomId}`);
    } catch (err) {
      alert(err.message);
    }
  };

  const handleLeave = async (roomId) => {
    try {
      await leaveRoom(roomId, user.uid);
      fetchRooms();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleSignOut = () => signOut(auth);

  if (!user) return <p>Loading...</p>;

  return (
    <div>
      <h2>Welcome, {user.email}</h2>
      <button onClick={handleSignOut}>Sign Out</button>
      <h3>Create Room</h3>
      <button onClick={handleCreate}>Create</button>

      <h3>Join Room</h3>
      <input value={joinRoomId} onChange={e => setJoinRoomId(e.target.value)} placeholder="Room ID" />
      <input value={joinPassword} onChange={e => setJoinPassword(e.target.value)} placeholder="Password" />
      <button onClick={handleJoin}>Join</button>

      <h3>Your Rooms</h3>
      <ul>
        {rooms.filter(r => r.participants?.some(p => p.id.userId === user.uid)).map(r => (
          <li key={r.roomId}>
            {r.roomId} - host: {r.hosterUserId}
            <button onClick={() => navigate(`/room/${r.roomId}`)}>Open</button>
            <button onClick={() => handleLeave(r.roomId)}>Leave</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
