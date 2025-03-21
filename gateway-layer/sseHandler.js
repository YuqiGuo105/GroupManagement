// sseHandler.js

const clients = [];

/**
 * Adds a new SSE client connection.
 * @param {Object} res - Express response object for the SSE connection.
 * @param {string|null} roomId - Optional room identifier for filtering events.
 */
const addClient = (res, roomId = null) => {
  clients.push({ res, roomId });
  console.log(`SSE client added for room: ${roomId || 'all'}. Total clients: ${clients.length}`);
};

/**
 * Removes a disconnected SSE client.
 * @param {Object} res - Express response object for the SSE connection.
 */
const removeClient = (res) => {
  const index = clients.findIndex(client => client.res === res);
  if (index !== -1) {
    clients.splice(index, 1);
    console.log('SSE client removed. Total clients:', clients.length);
  }
};

/**
 * Broadcasts a message to all connected SSE clients.
 * If a roomId is provided, only sends to clients in that room.
 * @param {Object|string} data - The data to send.
 * @param {string|null} roomId - Optional room identifier.
 */
const broadcast = (data, roomId = null) => {
  const payload = typeof data === 'string' ? data : JSON.stringify(data);
  clients.forEach(client => {
    // If a roomId is provided, only send to matching clients; otherwise, broadcast to all.
    if (!roomId || client.roomId === roomId) {
      client.res.write(`data: ${payload}\n\n`);
      // Flush the response if the flush method is available
      if (typeof client.res.flush === 'function') {
        client.res.flush();
      }
    }
  });
};

module.exports = {
  addClient,
  removeClient,
  broadcast,
};
