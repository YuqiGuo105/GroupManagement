// amqpConsumer.js
const amqp = require('amqplib');

const RABBIT_URL = process.env.RABBIT_URL || 'amqp://guest:guest@localhost:5672';
const ROOM_EXCHANGE = process.env.ROOM_EXCHANGE || 'roomExchange';
const ROUTING_KEY   = process.env.ROUTING_KEY   || 'room.events';
const QUEUE_NAME    = process.env.QUEUE_NAME    || 'gateway-queue';

async function startConsumer(io) {
  try {
    const connection = await amqp.connect(RABBIT_URL);
    const channel = await connection.createChannel();

    // Ensure the exchange exists; assuming a 'topic' exchange.
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
          // Parse the JSON payload from group-manage-service
          const payload = JSON.parse(content);
          // Expected payload format: { eventType: "USER_LEFT", roomId: "...", userId: "..." }
          if (payload && payload.roomId && payload.eventType) {
            // Broadcast the event to clients in the specified room
            io.to(payload.roomId).emit(payload.eventType, payload);
            console.log(`[Socket.IO] Emitted event "${payload.eventType}" to room "${payload.roomId}"`);
          } else {
            console.warn('[RabbitMQ] Payload missing roomId or eventType:', payload);
          }
          // Acknowledge the message
          channel.ack(msg);
        } catch (err) {
          console.error('[RabbitMQ] Error processing message:', err);
          // Acknowledge even on error to prevent requeue loop
          channel.ack(msg);
        }
      }
    }, { noAck: false });

    console.log('[RabbitMQ] Consumer started. Waiting for messages...');
  } catch (error) {
    console.error('[RabbitMQ] Connection/Consumption error:', error);
  }
}

module.exports = { startConsumer };
