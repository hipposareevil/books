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

    "time"

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
	// cache layer
	var cache CacheLayer
	cache = cacheLayer{redisPool}

    // clear the cache on startup
    cache.ClearAll(BOOK_CACHE)

	///////////////
	// 'book' service
	var bookSvc BookService
	bookSvc = bookService{db, cache}

	// Set up the endpoints on our service
	//
	// Note: the Authentication middleware is done on each endpoint
	// individually so we can tightly control each one as some
	// care about only accepting 'admin' group.

	////////////////
	// Endpoints

	//////
	// GET /book (all books)
	booksEndpoint := makeGetBooksEndpoint(bookSvc)
	baseBooksHandler := httptransport.NewServer(
		booksEndpoint,
		decodeGetAllBooksRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	booksHandler := Authenticate(false, redisPool, baseBooksHandler)
	router.Methods("GET").Path("/book").Handler(booksHandler)

	//////
	// GET /book/<book_id>
	bookEndpoint := makeGetBookEndpoint(bookSvc)
	baseBookHandler := httptransport.NewServer(
		bookEndpoint,
		decodeGetBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	bookHandler := Authenticate(false, redisPool, baseBookHandler)
	// 'book_id' is used in transport.go to grab the variable 'book_id' from the path
	router.Methods("GET").Path("/book/{book_id}").Handler(bookHandler)

	//////
	// DELETE /book/<book_id>
	deleteBookEndpoint := makeDeleteBookEndpoint(bookSvc)
	baseDeleteBookHandler := httptransport.NewServer(
		deleteBookEndpoint,
		decodeDeleteBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	deleteBookHandler := Authenticate(true, redisPool, baseDeleteBookHandler)
	// 'book_id' is used in transport.go to grab the variable 'book_id' from the path
	router.Methods("DELETE").Path("/book/{book_id}").Handler(deleteBookHandler)

	//////
	// POST /book
	createBookEndpoint := makeCreateBookEndpoint(bookSvc)
	baseCreateBookHandler := httptransport.NewServer(
		createBookEndpoint,
		decodeCreateBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	createBookHandler := Authenticate(true, redisPool, baseCreateBookHandler)
	router.Methods("POST").Path("/book").Handler(createBookHandler)

	//////
	// PUT /book/<book_id>
	updateBookEndpoint := makeUpdateBookEndpoint(bookSvc)
	baseUpdateBookHandler := httptransport.NewServer(
		updateBookEndpoint,
		decodeUpdateBookRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
	// first parameter denotes if only 'admin' group can access the endpoint.
	updateBookHandler := Authenticate(true, redisPool, baseUpdateBookHandler)
	// 'book_id' is used in transport.go to grab the variable 'book_id' from the path
	router.Methods("PUT").Path("/book/{book_id}").Handler(updateBookHandler)

	//////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	fmt.Println("book service up on " + addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
