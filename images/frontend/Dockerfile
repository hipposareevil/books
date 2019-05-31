FROM node:8.4.0-alpine

RUN apk update; apk upgrade; apk add git; apk add bash; apk add bash-completion
RUN sed -i s/ash/bash/ /etc/passwd
COPY bashrc /root/.bashrc

RUN npm install -g vue-cli

# caller should mount into /scratch to run the dev server
WORKDIR /scratch/

# Args passed in via 'docker build'
# Used by the LABELs
ARG BUILD_TIME
ARG VERSION

# Putting LABEL last so we can re-use the preceding caching layers
LABEL org.label-schema.build-date="$BUILD_TIME" \
      org.label-schema.vendor="github.com/hipposareevil" \
      org.label-schema.version="$VERSION" \
      org.label-schema.description="Dev Frontend SPA webpage for books application." \
      org.label-schema.name="books.frontend" 

#ENTRYPOINT ["npm", "run", "dev"]
CMD npm run dev


