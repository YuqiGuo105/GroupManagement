const amqp = require('amqplib');

const RABBIT_URL = process.env.RABBIT_URL || 'amqp://guest:guest@rabbitmq:5672';
const EXCHANGE = 'room-exchange';  // match your Spring config
const ROUTING_KEY = 'room.key';    // match your Spring config

async function startConsumer() {
  try {
    const connection = await amqp.connect(RABBIT_URL);
    const channel = await connection.createChannel();
    await channel.assertExchange(EXCHANGE, 'topic', { durable: true });

    // Create/bind your queue:
    const queueResult = await channel.assertQueue('gateway-queue', { durable: true });
    await channel.bindQueue(queueResult.queue, EXCHANGE, ROUTING_KEY);

    // Start consuming
    channel.consume(queueResult.queue, (msg) => {
      if (msg !== null) {
        const payloadStr = msg.content.toString();
        console.log('[RabbitMQ] Received:', payloadStr);
        channel.ack(msg);
      }
    }, { noAck: false });

    console.log('[RabbitMQ] Consumer started');
  } catch (err) {
    console.error('[RabbitMQ] Error:', err);
  }
}

module.exports = { startConsumer };
