# Basic nginx image with specific config and content
FROM nginx
COPY conf /etc/nginx

COPY content/* /usr/share/nginx/html/

VOLUME /etc/docker/certs

EXPOSE 80
EXPOSE 443
