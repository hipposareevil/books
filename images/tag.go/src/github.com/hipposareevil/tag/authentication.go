package main

// Authentication module for authenticating incoming REST calls against the
// redis DB using the 'authorization' Header that should contain 'Bearer: qerty-1234' 

import (
	"fmt"
    "net/http"
    "strings"
	"encoding/json"

	"github.com/mediocregopher/radix.v2/pool"
	_ "github.com/mediocregopher/radix.v2/redis"
)

// Authenticate a handler. This will return a 401 to the client if authentication has failed
// 
// params:
// onlyAdminGroup - true if only users belonging to the admin group can access the resource
//                  false if any authenticated user can access the resource
// redisPool - Pool to get redis connection
// next      - Handler to call if the authentication is successful
//
// return:
// a http.Handler
func Authenticate(onlyAdminGroup bool, redisPool *pool.Pool, next http.Handler) http.Handler {
    // return new handler that wraps 'next'
    return http.HandlerFunc(func(writer http.ResponseWriter, request *http.Request) {

        /////////////////
        // Redis
        conn, err := redisPool.Get()
        if err != nil {
            fmt.Println("Got error when calling pool.Get: ", err)
            writer.WriteHeader(http.StatusInternalServerError)
            return
        }
        defer redisPool.Put(conn)

        ///////////////////
        // Parse headers
        var bearer string
        fullBearerString := request.Header.Get("authorization")

        // Strip the 'Bearer ' from header
        if strings.HasPrefix(fullBearerString, "Bearer ") {
            bearer = strings.Replace(fullBearerString, "Bearer ", "", 1)
        }

        // Key to query in redis
        redisHashName := "user:" + bearer

        // Check redis. If it is null, there is no authentication
        userName, err := conn.Cmd("HGET", redisHashName, "name").Str()

        fmt.Println("Got username: ", userName)

        if err != nil {
            // No authorization -> send a 401
            writeUnauthorizedError(writer)
        } else {
            // If this endpoint is protected by group check for 'admin'
            if (onlyAdminGroup) {
                // Check for 'admin' group
                groupName, err := conn.Cmd("HGET", redisHashName, "group").Str()
                fmt.Println("Got groupName: ", groupName)

                if err != nil {
                    // No group authorization -> send a 401
                    writeUnauthorizedError(writer)
                } else {
                    // is group 'admin' ?
                    if (strings.Compare(groupName, "admin") == 0) {
                        // admin group
                        next.ServeHTTP(writer, request)
                    } else {
                        // not admin group
                        // Incorrect group authorization -> send a 401
                        writeUnauthorizedError(writer)
                    }
                }
                // done checking admin group
            } else {
                // Endpoint doesn't need to verify 'admin' group

                // User is authorized, continue as normal
                next.ServeHTTP(writer, request)
            }
        }
    })
}

// Write unauthorized error to the writer
func writeUnauthorizedError(writer http.ResponseWriter) {
    // send 401
    writer.Header().Set("Content-Type", "application/json; charset=utf-8")
    writer.WriteHeader(http.StatusUnauthorized)

    // Create JSON for the output
    jsonError := ErrorResponse {
        Code: http.StatusUnauthorized,
        Message: "Must supply valid Authorization header. Authenticate at /auth/token",
    }

    // Write out the JSON error
    json.NewEncoder(writer).Encode(jsonError)

}
