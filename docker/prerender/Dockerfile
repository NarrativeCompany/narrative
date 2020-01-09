FROM node:9

ENV NODE_PATH=/usr/local/lib/node_modules/

# Install Chrome.
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list
RUN apt-get update && apt-get -y install google-chrome-stable

RUN mkdir -p /opt/prerender

WORKDIR /opt/prerender

RUN npm install -G prerender-node
RUN npm install -G prerender
RUN npm install -G prerender-memory-cache

COPY server.js /opt/prerender/server.js

ENTRYPOINT ["node", "server.js"]

