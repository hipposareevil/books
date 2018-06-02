# Basic nginx image with specific config and content
FROM nginx
COPY conf /etc/nginx
COPY content/* /usr/share/nginx/html/

VOLUME /etc/docker/certs

# Args passed in via 'docker build'
# Used by the LABELs
ARG BUILD_TIME
ARG VERSION

# Putting LABEL last so we can re-use the preceding caching layers
LABEL org.label-schema.build-date="$BUILD_TIME" \
      org.label-schema.vendor="github.com/hipposareevil" \
      org.label-schema.version="$VERSION" \
      org.label-schema.description="Frontend proxy (API Gateway) to the microservices in the books project." \
      org.label-schema.name="books.gateway" 


EXPOSE 80
EXPOSE 443
