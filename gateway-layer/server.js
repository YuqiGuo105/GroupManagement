/**
 * server.js
 * Node gateway that:
 * 1) Hosts an Express + Socket.IO server.
 * 2) Connects to RabbitMQ and consumes events published by group-manage-service.
 * 3) Broadcasts those events to relevant Socket.IO rooms.
 */

const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const amqp = require('amqplib');

// --------------------------------------------------------------------------------
// Configuration
// --------------------------------------------------------------------------------
const PORT = process.env.PORT || 3000;
const RABBIT_URL = process.env.RABBIT_URL || 'amqp://guest:guest@localhost:5672';

// Must match the exchange and routing key used by group-manage-service
const ROOM_EXCHANGE = 'room-exchange';
const ROUTING_KEY   = 'room.key';

// Any queue name for this gateway's consumer
const QUEUE_NAME = 'gateway-queue';

// --------------------------------------------------------------------------------
// Create Express + HTTP + Socket.IO
// --------------------------------------------------------------------------------
const app = express();
app.use(express.json());

const server = http.createServer(app);
const io = new Server(server, {
  cors: { origin: '*' } // Adjust to your front-end's domain
});

// Basic route or health check (optional)
app.get('/', (req, res) => {
  res.send('Gateway is running');
});

// --------------------------------------------------------------------------------
// Socket.IO handlers
// --------------------------------------------------------------------------------
io.on('connection', (socket) => {
  console.log('[Socket.IO] Client connected:', socket.id);

  // Example: client sends { roomId: "abc123" } to join a Socket.IO room
  socket.on('joinRoom', (data) => {
    const { roomId } = data;
    socket.join(roomId);
    console.log(`[Socket.IO] Socket ${socket.id} joined room ${roomId}`);
  });

  // Example: client leaves a room
  socket.on('leaveRoom', (data) => {
    const { roomId } = data;
    socket.leave(roomId);
    console.log(`[Socket.IO] Socket ${socket.id} left room ${roomId}`);
  });

  // On disconnect
  socket.on('disconnect', () => {
    console.log('[Socket.IO] Client disconnected:', socket.id);
  });
});

// --------------------------------------------------------------------------------
// RabbitMQ Consumer
// --------------------------------------------------------------------------------
async function startConsumer() {
  try {
    const connection = await amqp.connect(RABBIT_URL);
    const channel = await connection.createChannel();

    // Ensure the exchange exists (must match what Spring Boot sets up, likely "topic")
    await channel.assertExchange(ROOM_EXCHANGE, 'topic', { durable: true });

    // Create or assert a queue for this gateway
    await channel.assertQueue(QUEUE_NAME, { durable: true });

    // Bind queue to the exchange with the same routing key used by group-manage-service
    await channel.bindQueue(QUEUE_NAME, ROOM_EXCHANGE, ROUTING_KEY);

    console.log(`[RabbitMQ] Bound queue "${QUEUE_NAME}" to exchange "${ROOM_EXCHANGE}" with RK "${ROUTING_KEY}"`);

    // Consume messages
    channel.consume(QUEUE_NAME, (msg) => {
      if (msg) {
        const content = msg.content.toString();
        console.log('[RabbitMQ] Received:', content);

        try {
          // Parse the JSON payload from group-manage-service
          const payload = JSON.parse(content);
          // payload might look like: { eventType: "USER_LEFT", roomId: "...", userId: "..." }

          // Broadcast the event to any Socket.IO clients in that room
          // e.g. io.to("abc123").emit("USER_LEFT", payload)
          io.to(payload.roomId).emit(payload.eventType, payload);

          // Acknowledge the message
          channel.ack(msg);

        } catch (err) {
          console.error('[RabbitMQ] Error handling message:', err);
          // Optionally: channel.nack(msg, false, false);
          channel.ack(msg); // ack anyway to avoid requeue loop
        }
      }
    }, { noAck: false });

    console.log('[RabbitMQ] Consumer started. Waiting for messages...');
  } catch (error) {
    console.error('[RabbitMQ] Failed to connect or consume:', error);
  }
}

// --------------------------------------------------------------------------------
// Proxy to group-manage-service
// If you also want to forward /api/* calls to your Spring Boot on port 8080
// --------------------------------------------------------------------------------
const axios = require('axios');
const SPRING_BOOT_URL = process.env.SPRING_BOOT_URL || 'http://localhost:8080';
app.all('/api/*', async (req, res) => {
  try {
    const subPath = req.originalUrl.replace('/api', '');
    const targetUrl = `${SPRING_BOOT_URL}/api${subPath}`;
    const response = await axios({
      method: req.method,
      url: targetUrl,
      headers: req.headers,
      data: req.body,
      params: req.query,
      validateStatus: () => true
    });
    res.status(response.status).send(response.data);
  } catch (error) {
    console.error('[Proxy Error]', error.message);
    res.status(500).json({ error: 'Proxy request failed', details: error.message });
  }
});

// --------------------------------------------------------------------------------
// Start the server & the RabbitMQ consumer
// --------------------------------------------------------------------------------
server.listen(PORT, () => {
  console.log(`Gateway listening on port ${PORT}`);
  startConsumer();
});
