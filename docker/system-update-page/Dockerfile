FROM nginx:stable

# Custom NGINX config
COPY ./default.conf /etc/nginx/conf.d/default.conf

COPY ./index.html /usr/share/nginx/html/index.html
COPY ./message.json /usr/share/nginx/html/message.json
COPY ./cartoon-robot.png /usr/share/nginx/html/cartoon-robot.png

