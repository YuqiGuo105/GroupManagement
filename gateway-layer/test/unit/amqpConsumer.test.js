const chai = require('chai');
const sinon = require('sinon');
const expect = chai.expect;
const amqp = require('amqplib');
const { startConsumer } = require('../../rabbitConsumer');

describe('AMQP Consumer', function() {
  let connectStub;
  let channelStub;
  let fakeConnection;

  beforeEach(function() {
    channelStub = {
      assertExchange: sinon.stub().resolves(),
      assertQueue: sinon.stub().resolves(),
      bindQueue: sinon.stub().resolves(),
      consume: sinon.stub().resolves(),
      ack: sinon.spy()
    };

    fakeConnection = {
      createChannel: sinon.stub().resolves(channelStub)
    };

    connectStub = sinon.stub(amqp, 'connect').resolves(fakeConnection);
  });

  afterEach(function() {
    sinon.restore();
  });

  it('should connect to RabbitMQ and create a channel', async function() {
    await startConsumer({ to: () => ({ emit: () => {} }) });
    expect(connectStub.called).to.be.true;
    expect(fakeConnection.createChannel.called).to.be.true;
  });

  it('should assert exchange and bind queue', async function() {
    await startConsumer({ to: () => ({ emit: () => {} }) });
    expect(channelStub.assertExchange.called).to.be.true;
    expect(channelStub.bindQueue.called).to.be.true;
  });

  it('should call consume on the queue', async function() {
    await startConsumer({ to: () => ({ emit: () => {} }) });
    expect(channelStub.consume.called).to.be.true;
  });
});
