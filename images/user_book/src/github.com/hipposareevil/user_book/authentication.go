package main

// Authentication module for authenticating incoming REST calls against the
// redis DB using the 'authorization' Header that should contain 'Bearer: qerty-1234'

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strings"

	"github.com/mediocregopher/radix.v2/pool"
	_ "github.com/mediocregopher/radix.v2/redis"
)

// Authenticate a handler. This will return a 401 to the client if authentication has failed
//
// Any user in the 'admin' usergroup will have access to the resource.
//
// params:
// onlyMatchingUser - true:  the authorized user's ID must match the ID in the resource URL
//                    false: any authorized user can access the resource
// redisPool - Pool to get redis connection
// next      - Handler to call if the authentication is successful
//
// return:
// a http.Handler
func AuthenticateForUserBook(onlyMatchingUser bool, redisPool *pool.Pool, next http.Handler) http.Handler {
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
		// Parse url
		userIdInUrl, err := parseUserId(request)

		if err != nil {
			// unable to get the userid from the request
			fmt.Println("Unable to get user-id from redis:", err)
			writeUnauthorizedError(writer)
		}

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

		// Check redis. If it is null, authentication failed
		_, err = conn.Cmd("HGET", redisHashName, "name").Str()

		if err != nil {
			// No authorization -> send a 401
			fmt.Println("Unable to fine data in redis for name:", redisHashName)
			writeUnauthorizedError(writer)
		} else {
			// get data from redis

			// group name
			groupName, err := conn.Cmd("HGET", redisHashName, "group").Str()
			if err != nil {
				// No group authorization -> send a 401
				writeUnauthorizedError(writer)
			}

			// user id
			userIdFromRedis, err := conn.Cmd("HGET", redisHashName, "id").Int()
			if err != nil {
				// No group authorization -> send a 401
				writeUnauthorizedError(writer)
			}

			// If the authenticated user is an 'admin' OR
			// (when onlyMatchingUser is true) the authenticated user ID is the same as in the URL
			if strings.Compare(groupName, "admin") == 0 {
				// In admin group, go ahead
				next.ServeHTTP(writer, request)
			} else if onlyMatchingUser {
				// Check user ids

				if userIdFromRedis == userIdInUrl {
					// matching ids, go ahead
					next.ServeHTTP(writer, request)
				} else {
					// IDs don't match, error out
					writeUnauthorizedError(writer)
				}
			} else {
				// No need to check ids
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
	jsonError := ErrorResponse{
		Code:    http.StatusUnauthorized,
		Message: "Must supply valid Authorization header. Authenticate at /auth/token",
	}

	// Write out the JSON error
	json.NewEncoder(writer).Encode(jsonError)

}
