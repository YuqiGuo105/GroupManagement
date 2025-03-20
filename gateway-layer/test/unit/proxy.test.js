const chai = require('chai');
const expect = chai.expect;
const proxy = require('../../proxy');

describe('Proxy Middleware', function() {
  it('should return a valid proxy configuration', function() {
    const config = proxy.getProxyConfig();
    expect(config).to.be.an('object');
    expect(config).to.have.property('target');
    expect(config).to.have.property('changeOrigin', true);
  });

  it('should create a middleware function', function() {
    const middleware = proxy.createProxy();
    expect(middleware).to.be.a('function');
  });
});
