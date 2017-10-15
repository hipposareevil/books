FROM node:8.4.0-alpine

RUN apk update; apk upgrade; apk add git; apk add bash; apk add bash-completion
RUN sed -i s/ash/bash/ /etc/passwd
COPY bashrc /root/.bashrc

RUN npm install -g vue-cli

# caller should mount into /scratch to run the dev server
WORKDIR /scratch/

ENTRYPOINT ["npm", "run", "dev"]


