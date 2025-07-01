const API_BASE = '/api';

export async function createRoom(hoster) {
  const res = await fetch(`${API_BASE}/rooms/create?hoster=${hoster}`, {
    method: 'POST'
  });
  if (!res.ok) throw new Error('Failed to create room');
  return res.json();
}

export async function joinRoom(roomId, password, userId) {
  const res = await fetch(`${API_BASE}/rooms/join?roomId=${roomId}&password=${password}&userId=${userId}`, { method: 'POST' });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}

export async function leaveRoom(roomId, userId) {
  const res = await fetch(`${API_BASE}/rooms/leave?roomId=${roomId}&userId=${userId}`, { method: 'POST' });
  if (!res.ok) throw new Error(await res.text());
  return res.text();
}

export const getRoom = async (roomId) => {
  const res = await fetch(`${API_BASE}/rooms/${roomId}`);
  if (!res.ok) throw new Error('Failed to fetch room');
  return res.json();
};

export const getAllRooms = async () => {
  const res = await fetch(`${API_BASE}/rooms`);
  if (!res.ok) throw new Error('Failed to fetch rooms');
  return res.json();
};
