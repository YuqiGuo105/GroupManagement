# Dockerfile inside gateway-layer/
FROM node:18-alpine

LABEL author = "yuqi.guo17@gmail.com"

# Create app directory
WORKDIR /usr/src/app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of your code
COPY . .

# Expose the port your app runs on (adjust if needed)
EXPOSE 3000

# Start the app
CMD ["npm", "start"]
