FROM node:18-alpine

LABEL author = "yuqi.guo17@gmail.com"

WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
