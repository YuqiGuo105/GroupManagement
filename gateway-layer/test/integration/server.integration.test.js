// test/integration/server.integration.test.js

const chai = require('chai');
const expect = chai.expect;
const request = require('supertest');
const http = require('http');
const app = require('../../app');  // Ensure you are requiring your app (not the server that calls app.listen)

describe('Express Server', function() {
  let server;
  let port;

  before(function(done) {
    // Create a server instance using the app and listen on an ephemeral port
    server = http.createServer(app);
    server.listen(0, function() {
      port = server.address().port;
      done();
    });
  });

  after(function(done) {
    server.close(done);
  });

  it('should respond to GET / with plain text', function(done) {
    request(server)
      .get('/')
      .expect(200)
      .end(function(err, res) {
        if (err) return done(err);
        // Use the correct variable "res" (not "response") and Chai's "to.contain"
        expect(res.text).to.contain('Gateway is running');
        done();
      });
  });

  it('should proxy /api requests and return 404/504', async function() {
    // Using the same server instance so that proxy routes are available
    const res = await request(server).get('/api/somepath');
    expect([404, 504]).to.include(res.status);
  });
});
