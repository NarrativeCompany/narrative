// server.js
const prerender = require('prerender');
var server = prerender({
  chromeFlags: [ ' --disable-dev-shm-usage', ' --no-sandbox', ' --headless', ' --disable-gpu', ' --remote-debugging-port=9222', ' --hide-scrollbars' ],
  workers: 4,
  logRequests: true
});

server.use(require('prerender-memory-cache'))
server.use(prerender.sendPrerenderHeader());
// server.use(prerender.blockResources());
server.use(prerender.removeScriptTags());
server.use(prerender.httpHeaders());

server.start();
