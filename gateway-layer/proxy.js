// proxy.js
const { createProxyMiddleware } = require('http-proxy-middleware');
const SPRING_BOOT_URL = process.env.SPRING_BOOT_URL || 'http://localhost:8080';

function getProxyConfig() {
  return {
    target: SPRING_BOOT_URL,
    changeOrigin: true,
    pathRewrite: { '^/api': '/api' },
    logLevel: 'debug',
    timeout: 500, // Short timeout to trigger gateway timeout quickly
    onError: (err, req, res) => {
      res.writeHead(504, {
        'Content-Type': 'text/plain'
      });
      res.end('Gateway Timeout');
    }
  };
}

function createProxy() {
  return createProxyMiddleware(getProxyConfig());
}

module.exports = { getProxyConfig, createProxy };
