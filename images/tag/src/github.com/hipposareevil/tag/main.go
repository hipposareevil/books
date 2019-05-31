package main

// Main application
//
// This will create the databases, router, static files 
// and wire everything together 


import (
	"fmt"
    "time"
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
	// cache layer
	var cache CacheLayer
	cache = cacheLayer{redisPool}

    // clear the cache as we are starting fresh
    cache.ClearAll(TAG_CACHE)

    ///////////////
    // 'tag' service
    var tagSvc TagService
    tagSvc = tagService{db, cache}
    
    // Add middleware here

    // Set up the endpoints on our service
    // Note the Authentication middleware is done on each endpoint
    // individually so we can tightly control each one.


    ////////////////
    // Endpoints

	// GET /tag
    tagsEndpoint := makeTagsEndpoint(tagSvc)
	baseTagsHandler := httptransport.NewServer(
        tagsEndpoint,
		decodeTagsRequest,
		encodeResponse,
	)
    // Add middleware to authenticate the endpoint.
    // first parameter denotes if only 'admin' group can access the endpoint.
    tagsHandler := Authenticate(false, redisPool, baseTagsHandler)
	router.Methods("GET").Path("/tag").Handler(tagsHandler)

	// GET /tag/<tag_id> 
    tagEndpoint := makeTagEndpoint(tagSvc)
	baseTagHandler := httptransport.NewServer(
        tagEndpoint,
		decodeTagRequest,
		encodeResponse,
	)
    // Add middleware to authenticate the endpoint.
    // first parameter denotes if only 'admin' group can access the endpoint.
    tagHandler := Authenticate(false, redisPool, baseTagHandler)
	// 'tag_id' is used in transport.go to grab the variable 'tag_id' from the path
	router.Methods("GET").Path("/tag/{tag_id}").Handler(tagHandler)

	// PUT /tag/<tag_id>
    putTagEndpoint := makePutTagEndpoint(tagSvc)
	basePutTagHandler := httptransport.NewServer(
		putTagEndpoint,
		decodePutTagRequest,
		encodeResponse,
	)
    // Add middleware to authenticate the endpoint.
    // first parameter denotes if only 'admin' group can access the endpoint.
    putTagHandler := Authenticate(true, redisPool, basePutTagHandler)
	// 'tag_id' is used in transport.go to grab the variable 'tag_id' from the path
	router.Methods("PUT").Path("/tag/{tag_id}").Handler(putTagHandler)

	// POST /tag
    postTagEndpoint := makePostTagEndpoint(tagSvc)
	basePostTagHandler := httptransport.NewServer(
		postTagEndpoint,
		decodePostTagRequest,
		encodeResponse,
	)
    // Add middleware to authenticate the endpoint.
    // first parameter denotes if only 'admin' group can access the endpoint.
    postTagHandler := Authenticate(true, redisPool, basePostTagHandler)
	router.Methods("POST").Path("/tag").Handler(postTagHandler)

	// DELETE /tag
	// This reuses 'decodeTagRequest' as it is the same for GET and DELETE
    deleteTagEndpoint := makeDeleteTagEndpoint(tagSvc)
	baseDeleteTagHandler := httptransport.NewServer(
		deleteTagEndpoint,
		decodeTagRequest,
		encodeResponse,
	)
    // Add middleware to authenticate the endpoint.
    // first parameter denotes if only 'admin' group can access the endpoint.
    deleteTagHandler := Authenticate(true, redisPool, baseDeleteTagHandler)
	// 'tag_id' is used in transport.go to grab the variable 'tag_id' from the path
    router.Methods("DELETE").Path("/tag/{tag_id}").Handler(deleteTagHandler)

    //////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
