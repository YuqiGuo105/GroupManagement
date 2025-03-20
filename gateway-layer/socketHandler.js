// socketHandler.js
module.exports = function(io) {
  io.on('connection', (socket) => {
    console.log('[Socket.IO] Client connected:', socket.id);

    // When a client wants to join a room
    socket.on('joinRoom', (data) => {
      const { roomId } = data;
      if (roomId) {
        socket.join(roomId);
        console.log(`[Socket.IO] Socket ${socket.id} joined room ${roomId}`);
      }
    });

    // When a client wants to leave a room
    socket.on('leaveRoom', (data) => {
      const { roomId } = data;
      if (roomId) {
        socket.leave(roomId);
        console.log(`[Socket.IO] Socket ${socket.id} left room ${roomId}`);
      }
    });

    // When a client signals a host change event
    socket.on('hostChange', (data) => {
      const { roomId, newHost } = data;
      if (roomId && newHost) {
        // Broadcast the host change to all clients in the room
        io.to(roomId).emit('hostChange', { newHost });
        console.log(`[Socket.IO] Host changed in room ${roomId} to ${newHost}`);
      } else {
        console.warn('[Socket.IO] hostChange event missing roomId or newHost:', data);
      }
    });

    socket.on('disconnect', () => {
      console.log('[Socket.IO] Client disconnected:', socket.id);
    });
  });
};
