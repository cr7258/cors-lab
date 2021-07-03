const http = require('http');
const PORT = 30011;

http.createServer((req, res) => {
  const url = req.url;
  console.log('request url: ', url);
  if (url === '/api/data') {
    return res.end('hello world');
  }
}).listen(PORT);

console.log('Server listening on port ', PORT);
