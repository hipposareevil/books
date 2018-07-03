package main

// Main application
//
// This will create the databases, router, static files
// and wire everything together

import (
	"fmt"
	"net/http"
	"os"

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
	redisPool, err := pool.New("tcp", "token_db:6379", 10)
	if err != nil {
		fmt.Println("Got error when making connection to redis: ", err)
	}

	/////////////////
	// Make Mysql db connection
	db, err := sql.Open("mysql", "booksuser:books@tcp(books_db:3306)/booksdatabase?parseTime=true")

	// if there is an error opening the connection, handle it
	if err != nil {
		panic(err.Error())
	}
	defer db.Close()

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
	// cache layer
	var cache CacheLayer
	cache = cacheLayer{redisPool}

	///////////////
	// 'userbook' service
	var userbookSvc UserBookService
	userbookSvc = userbookService{db, cache}

	// Set up the endpoints on our service
	//
	// Note: the Authentication middleware is done on each endpoint
	// individually so we can tightly control each one as some
	// care about only accepting 'admin' group.

	////////////////
	// Endpoints

	//////
	// GET /user_book/<user_id>   (all books)
	userBooksEndpoint := makeGetUserBooksEndpoint(userbookSvc)
	baseUserBooksHandler := httptransport.NewServer(
		userBooksEndpoint,
		decodeGetAllUserBooksRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	userBooksHandler := Authenticate(false, redisPool, baseUserBooksHandler)
	// The id is used in transport.go to grab the variable 'book_id' from the path
	router.Methods("GET").Path("/user_book/{user_id}").Handler(userBooksHandler)

	//////
	// GET /user_book/<user_id>/<user_book_id>
	userBookEndpoint := makeGetUserBookEndpoint(userbookSvc)
	baseBookHandler := httptransport.NewServer(
		userBookEndpoint,
		decodeGetUserBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	userBookHandler := Authenticate(false, redisPool, baseBookHandler)
	// The id's are used in transport.go to grab the variable 'book_id' from the path
	router.Methods("GET").Path("/user_book/{user_id}/{user_book_id}").Handler(userBookHandler)

	//////
	// DELETE /user_book/<user_id>/<user_book_id>
	deleteUserBookEndpoint := makeDeleteUserBookEndpoint(userbookSvc)
	baseDeleteUserBookHandler := httptransport.NewServer(
		deleteUserBookEndpoint,
		decodeDeleteUserBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	deleteUserBookHandler := Authenticate(true, redisPool, baseDeleteUserBookHandler)
	// The id's are used in transport.go to grab the variable 'book_id' from the path
	router.Methods("DELETE").Path("/user_book/{user_id}/{user_book_id}").Handler(deleteUserBookHandler)

	//////
	// POST /user_book/<user_id>
	createUserBookEndpoint := makeCreateUserBookEndpoint(userbookSvc)
	baseCreateUserBookHandler := httptransport.NewServer(
		createUserBookEndpoint,
		decodeCreateUserBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	createUserBookHandler := Authenticate(true, redisPool, baseCreateUserBookHandler)
	router.Methods("POST").Path("/user_book/{user_id}").Handler(createUserBookHandler)

	//////
	// PUT /user_book/<user_id>/<user_book_id>
	updateUserBookEndpoint := makeUpdateUserBookEndpoint(userbookSvc)
	baseUpdateUserBookHandler := httptransport.NewServer(
		updateUserBookEndpoint,
		decodeUpdateUserBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	updateUserBookHandler := Authenticate(true, redisPool, baseUpdateUserBookHandler)
	// The id's are used in transport.go to grab the variable 'book_id' from the path
	router.Methods("PUT").Path("/user_book/{user_id}/{user_book_id}").Handler(updateUserBookHandler)

	//////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	fmt.Println("book service up on " + addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
