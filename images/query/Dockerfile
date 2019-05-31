########
# Build stage
FROM golang:alpine AS build-env

# golang defaults to directory '/go' for building
WORKDIR /go

# add 'src' and 'pkg' into the /go directory
COPY src/ /go/src/

# Builds the 'query' service and places it into /go
# Note the extra flags passed to 'go build' so this can run on a scratch image instead of alpine.
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo github.com/hipposareevil/query

########
# final stage
#FROM scratch
FROM alpine:3.8

# Add in certs for SSL connections
RUN apk add  --no-cache ca-certificates

# Args passed in via 'docker build'
# Used by the LABELs
ARG BUILD_TIME
ARG VERSION

# copy binary
COPY --from=build-env /go/query /

# copy static html files
COPY html/* /html/

# Putting LABEL last so we can re-use the preceding caching layers
LABEL org.label-schema.build-date="$BUILD_TIME" \
      org.label-schema.vendor="github.com/hipposareevil" \
      org.label-schema.version="$VERSION" \
      org.label-schema.description="Microservice for querying 3rd party APIs for author and book information." \
      org.label-schema.name="books.query.go" 

CMD ["/query"]

