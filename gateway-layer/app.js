// app.js
const express = require('express');
const { createProxy } = require('./proxy');

const app = express();
app.use(express.json());

// Health check route
app.get('/', (req, res) => {
  res.send('Gateway is running');
});

// API Gateway: Proxy /api/* calls to the Spring Boot backend
app.use('/api', createProxy());

module.exports = app;
