// server.js

const express = require('express');
const cors = require('cors')
const app = express();
const port = process.env.PORT || 3000;
const sseHandler = require('./sseHandler');

const allowedOrigins = process.env.CORS_ORIGIN?.split(',') || [];

app.use(cors({
  origin: function (origin, callback) {
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  }
}));

// Start the RabbitMQ consumer by requiring amqpConsumer.js
require('./rabbitConsumer');

// Add proxy middleware for /api routes
const { createProxy } = require('./proxy');
app.use('/api', createProxy());

// SSE endpoint: Clients connect here and may specify a room using the query parameter `room`
app.get('/events', (req, res) => {
  // Set headers required for SSE
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  // Send an initial comment to keep the connection open
  res.write(': connected\n\n');

  // Get the room id from query parameters (if provided)
  const roomId = req.query.room || null;

  // Add the client to the SSE handler with the room id
  sseHandler.addClient(res, roomId);

  // Remove the client when the connection closes
  req.on('close', () => {
    sseHandler.removeClient(res);
  });
});

// Basic route for testing the server
app.get('/', (req, res) => {
  res.send('Gateway is running');
});

app.listen(port, () => {
  console.log(`Server listening on port ${port}`);
});
