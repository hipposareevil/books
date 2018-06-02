########
# Build stage
FROM golang:alpine AS build-env

# Args passed in via 'docker build'
# Used by the LABELs
ARG BUILD_TIME
ARG VERSION

# golang defaults to directory '/go' for building
WORKDIR /go

# add 'src' and 'pkg' into the /go directory
COPY src/ /go/src/

# Builds the 'tag' service and places it into /go
# Note the extra flags passed to 'go build' so this can run on a scratch image instead of alpine.
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo github.com/hipposareevil/tag

########
# final stage
FROM scratch

# copy binary
COPY --from=build-env /go/tag /

# copy static html files
COPY html/* /html/

# Putting LABEL last so we can re-use the preceding caching layers
LABEL org.label-schema.build-date="$BUILD_TIME" \
      org.label-schema.vendor="github.com/hipposareevil" \
      org.label-schema.version="$VERSION" \
      org.label-schema.description="Microservice for managing tags in the project." \
      org.label-schema.name="books.tag.go" 

CMD ["/tag"]

