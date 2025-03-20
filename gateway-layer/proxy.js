// proxy.js
const { createProxyMiddleware } = require('http-proxy-middleware');
const SPRING_BOOT_URL = process.env.SPRING_BOOT_URL || 'http://localhost:8080';

function getProxyConfig() {
  return {
    target: SPRING_BOOT_URL,
    changeOrigin: true,
    pathRewrite: { '^/api': '/api' },
    logLevel: 'debug'
  };
}

function createProxy() {
  return createProxyMiddleware(getProxyConfig());
}

module.exports = { getProxyConfig, createProxy };
