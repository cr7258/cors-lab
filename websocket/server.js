const PORT = 3011;
const WebSocket = require("ws");
const server = new WebSocket.Server({ port: PORT });
server.on("connection", function(socket) {
  socket.on("message", function(data) {
    socket.send("Hello Client");
  });
});
console.log('Server listening on port ', PORT);
