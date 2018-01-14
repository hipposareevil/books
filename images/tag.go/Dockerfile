########
# Build stage
FROM golang:alpine AS build-env

# golang defaults to directory '/go' for building
WORKDIR /go

# add 'src' and 'pkg' into the /go directory
COPY src/ /go/src/

# Builds the 'tag' service and places it into /go
# Note the extra flags passed to 'go build' so this can run on a scratch image instead of alpine
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo github.com/hipposareevil/tag

########
# final stage
FROM scratch

# copy binary
COPY --from=build-env /go/tag /

# copy static html files
COPY html/* /html/

CMD ["/tag"]

