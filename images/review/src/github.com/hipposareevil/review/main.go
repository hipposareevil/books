package main

// Main application
//
// This will create the databases, router, static files
// and wire everything together

import (
	"fmt"
	"net/http"
	"os"

	// redis
	"github.com/mediocregopher/radix.v2/pool"

	// gorilla routing
	"github.com/gorilla/mux"

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
	// 'review' service
	var reviewSvc ReviewService
	reviewSvc = reviewService{}

	// Set up the endpoints on our service
	//
	// Note: the Authentication middleware is done on each endpoint
	// individually so we can tightly control each one as some
	// care about only accepting 'admin' group.

	////////////////
	// Endpoints

	//////
	// GET /review/<book_id>   
	reviewsEndpoint := makeGetReviewsEndpoint(reviewSvc)
	baseReviewsHandler := httptransport.NewServer(
		reviewsEndpoint,
		decodeGetReviewsRequest,
		encodeResponse,
	)
	// Add middleware to authenticate the endpoint.
    // first parameter: When true, the authenticated user's ID must match the userid in the url
	reviewsHandler := Authenticate(false, redisPool, baseReviewsHandler)
	// The id is used in transport.go to grab the variable 'book_id' from the path
	router.Methods("GET").Path("/review/{book_id}").Handler(reviewsHandler)
    
	//////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	fmt.Println("reviews service up on " + addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
