// amqpConsumer.js

const amqp = require('amqplib');
const sseHandler = require('./sseHandler');

const RABBIT_URL = process.env.RABBIT_URL || 'amqp://guest:guest@localhost:5672';
const ROOM_EXCHANGE = process.env.ROOM_EXCHANGE || 'roomExchange';
const ROUTING_KEY   = process.env.ROUTING_KEY   || 'room.events';
const QUEUE_NAME    = process.env.QUEUE_NAME    || 'gateway-queue';

async function startConsumer() {
  try {
    const connection = await amqp.connect(RABBIT_URL);
    const channel = await connection.createChannel();

    // Ensure the exchange exists (assuming a 'topic' exchange)
    await channel.assertExchange(ROOM_EXCHANGE, 'topic', { durable: true });

    // Ensure the queue exists
    await channel.assertQueue(QUEUE_NAME, { durable: true });

    // Bind the queue to the exchange with the specified routing key
    await channel.bindQueue(QUEUE_NAME, ROOM_EXCHANGE, ROUTING_KEY);
    console.log(`[RabbitMQ] Bound queue "${QUEUE_NAME}" to exchange "${ROOM_EXCHANGE}" with routing key "${ROUTING_KEY}"`);

    // Consume messages from the queue
    channel.consume(QUEUE_NAME, (msg) => {
      if (msg) {
        const content = msg.content.toString();
        console.log('[RabbitMQ] Received message:', content);

        try {
          // Parse the JSON payload
          const payload = JSON.parse(content);
          // Expected payload format: { eventType: "USER_LEFT", roomId: "...", userId: "..." }
          if (payload && payload.roomId && payload.eventType) {
            // Broadcast the event to clients subscribed to the specified room
            sseHandler.broadcast(payload, payload.roomId);
            console.log(`[SSE] Emitted event "${payload.eventType}" to room "${payload.roomId}"`);
          } else {
            console.warn('[RabbitMQ] Payload missing roomId or eventType:', payload);
          }
          // Acknowledge the message
          channel.ack(msg);
        } catch (err) {
          console.error('[RabbitMQ] Error processing message:', err);
          // Acknowledge the message to prevent a requeue loop
          channel.ack(msg);
        }
      }
    }, { noAck: false });

    console.log('[RabbitMQ] Consumer started. Waiting for messages...');
  } catch (error) {
    console.error('[RabbitMQ] Connection/Consumption error:', error);
  }
}

startConsumer();

module.exports = { startConsumer };
