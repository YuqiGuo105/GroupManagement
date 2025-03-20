// server.js
const http = require('http');
const { Server } = require('socket.io');
const app = require('./app');
const socketHandler = require('./socketHandler');
const { startConsumer } = require('./rabbitConsumer');

const PORT = process.env.PORT || 3000;

// Create HTTP server
const server = http.createServer(app);

// Create Socket.IO server instance with CORS settings for your frontend
const io = new Server(server, {
  cors: { origin: '*' } // Adjust this to your frontend's domain in production
});

// Setup Socket.IO event handlers
socketHandler(io);

// Start HTTP server
server.listen(PORT, () => {
  console.log(`Gateway listening on port ${PORT}`);
  // Start RabbitMQ consumer and pass Socket.IO instance
  startConsumer(io);
});

module.exports = { server, io, app };
