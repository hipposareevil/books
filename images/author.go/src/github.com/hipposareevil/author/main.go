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
	db, err := sql.Open("mysql", "booksuser:books@tcp(books_db:3306)/booksdatabase")

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

    // Clear the cache on startup
    cache.ClearAll(AUTHOR_CACHE)

	///////////////
	// 'author' service
	var authorSvc AuthorService
	authorSvc = authorService{db, cache}

	// Set up the endpoints on our service
	//
	// Note: the Authentication middleware is done on each endpoint
	// individually so we can tightly control each one as some
	// care about only accepting 'admin' group.

	////////////////
	// Endpoints

	//////
	// GET /author (all authors)
	authorsEndpoint := makeGetAuthorsEndpoint(authorSvc)
	baseAuthorsHandler := httptransport.NewServer(
		authorsEndpoint,
		decodeGetAllAuthorsRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	authorsHandler := Authenticate(false, redisPool, baseAuthorsHandler)
	router.Methods("GET").Path("/author").Handler(authorsHandler)

	//////
	// GET /author/<author_id>
	authorEndpoint := makeGetAuthorEndpoint(authorSvc)
	baseAuthorHandler := httptransport.NewServer(
		authorEndpoint,
		decodeGetAuthorRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	authorHandler := Authenticate(false, redisPool, baseAuthorHandler)
	// 'author_id' is used in transport.go to grab the variable 'author_id' from the path
	router.Methods("GET").Path("/author/{author_id}").Handler(authorHandler)

	//////
	// DELETE /author/<author_id>
	deleteAuthorEndpoint := makeDeleteAuthorEndpoint(authorSvc)
	baseDeleteAuthorHandler := httptransport.NewServer(
		deleteAuthorEndpoint,
		decodeDeleteAuthorRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	deleteAuthorHandler := Authenticate(true, redisPool, baseDeleteAuthorHandler)
	// 'author_id' is used in transport.go to grab the variable 'author_id' from the path
	router.Methods("DELETE").Path("/author/{author_id}").Handler(deleteAuthorHandler)

	//////
	// POST /author
	createAuthorEndpoint := makeCreateAuthorEndpoint(authorSvc)
	baseCreateAuthorHandler := httptransport.NewServer(
		createAuthorEndpoint,
		decodeCreateAuthorRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	createAuthorHandler := Authenticate(true, redisPool, baseCreateAuthorHandler)
	router.Methods("POST").Path("/author").Handler(createAuthorHandler)

	//////
	// PUT /author/<author_id>
	updateAuthorEndpoint := makeUpdateAuthorEndpoint(authorSvc)
	baseUpdateAuthorHandler := httptransport.NewServer(
		updateAuthorEndpoint,
		decodeUpdateAuthorRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	updateAuthorHandler := Authenticate(true, redisPool, baseUpdateAuthorHandler)
	// 'author_id' is used in transport.go to grab the variable 'author_id' from the path
	router.Methods("PUT").Path("/author/{author_id}").Handler(updateAuthorHandler)

	//////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	fmt.Println("author service up on " + addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
