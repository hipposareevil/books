package main

// Main application
//
// This will create the databases, router, static files
// and wire everything together

import (
	"fmt"
	"net/http"
	"os"
	"time"

	// mysql
	"database/sql"
	_ "github.com/go-sql-driver/mysql"

	// redis
	"github.com/mediocregopher/radix.v2/pool"

	// gorilla routing
	"github.com/gorilla/mux"

	// metrics

	"github.com/go-kit/kit/log"
	httptransport "github.com/go-kit/kit/transport/http"
)

// Main
func main() {
	logger := log.NewLogfmtLogger(os.Stderr)

	/////////////////
	// Make redis pool
	redisPool, err := pool.New("tcp", "books.token_db:6379", 10)
	if err != nil {
		fmt.Println("Got error when making connection to redis: ", err)
	}

	/////////////////
	// Make Mysql db connection
	db, err := sql.Open("mysql", "booksuser:books@tcp(books.db:3306)/booksdatabase")

	// if there is an error opening the connection, handle it
	if err != nil {
		panic(err.Error())
	}
	defer db.Close()
	db.SetMaxIdleConns(0)
	db.SetConnMaxLifetime(time.Second * 10)

	///////////////////
	// create services and endpoints

	/////////
	// ROUTER
	router := mux.NewRouter()
	// Make gorilla be router for everything
	http.Handle("/", router)

	/////////////////
	// Swagger static html file
	htmlDir := "/html"

	// Create server for swagger file
	fs := http.FileServer(http.Dir(htmlDir))
	router.PathPrefix("/swagger.yaml").Handler(http.StripPrefix("/", fs))

	///////////////
	// 'user' service
	var userSvc UserService
	userSvc = userService{db}

	// Set up the endpoints on our service
	//
	// Note: the Authentication middleware is done on each endpoint
	// individually so we can tightly control each one as some
	// care about only accepting 'admin' group.

	////////////////
	// Endpoints

	//////
	// GET /user (all users)
	usersEndpoint := makeGetUsersEndpoint(userSvc)
	baseUsersHandler := httptransport.NewServer(
		usersEndpoint,
		decodeGetAllUsersRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	usersHandler := Authenticate(false, redisPool, baseUsersHandler)
	router.Methods("GET").Path("/user").Handler(usersHandler)

	//////
	// GET /user/<user_id>
	userEndpoint := makeGetUserEndpoint(userSvc)
	baseUserHandler := httptransport.NewServer(
		userEndpoint,
		decodeGetUserRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	userHandler := Authenticate(false, redisPool, baseUserHandler)
	// 'user_id' is used in transport.go to grab the variable 'user_id' from the path
	router.Methods("GET").Path("/user/{user_id}").Handler(userHandler)

	//////
	// DELETE /user/<user_id>
	deleteUserEndpoint := makeDeleteUserEndpoint(userSvc)
	baseDeleteUserHandler := httptransport.NewServer(
		deleteUserEndpoint,
		decodeDeleteUserRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	deleteUserHandler := Authenticate(true, redisPool, baseDeleteUserHandler)
	// 'user_id' is used in transport.go to grab the variable 'user_id' from the path
	router.Methods("DELETE").Path("/user/{user_id}").Handler(deleteUserHandler)

	//////
	// POST /user
	createUserEndpoint := makeCreateUserEndpoint(userSvc)
	baseCreateUserHandler := httptransport.NewServer(
		createUserEndpoint,
		decodeCreateUserRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	createUserHandler := Authenticate(true, redisPool, baseCreateUserHandler)
	router.Methods("POST").Path("/user").Handler(createUserHandler)

	//////
	// PUT /user/<user_id>
	updateUserEndpoint := makeUpdateUserEndpoint(userSvc)
	baseUpdateUserHandler := httptransport.NewServer(
		updateUserEndpoint,
		decodeUpdateUserRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	updateUserHandler := Authenticate(true, redisPool, baseUpdateUserHandler)
	// 'user_id' is used in transport.go to grab the variable 'user_id' from the path
	router.Methods("PUT").Path("/user/{user_id}").Handler(updateUserHandler)

	//////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	fmt.Println("user service up on " + addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
