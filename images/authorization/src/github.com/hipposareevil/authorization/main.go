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

	"github.com/go-kit/kit/log"
	httptransport "github.com/go-kit/kit/transport/http"

	// bcrypt
	_ "golang.org/x/crypto/bcrypt"
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
	// 'authorization' service
	var authorizeSvc AuthorizeService
	authorizeSvc = authorizeService{
		db,
		redisPool,
	}

	// Set up the endpoints on our service

	////////////////
	// Endpoints

	////////////
	// #1
	// GET /authorize/validate
	validateEndpoint := makeGetValidationEndpoint(authorizeSvc)
	baseValidateHandler := httptransport.NewServer(
		validateEndpoint,
		decodeValidationRequest,
		encodeResponse,
	)
	router.Methods("GET").Path("/authorize/validate").Handler(baseValidateHandler)

	////////////
	// #2
	// POST /authorize/token
	createTokenEndpoint := makeCreateTokenEndpoint(authorizeSvc)
	baseCreateTokenHandler := httptransport.NewServer(
		createTokenEndpoint,
		decodeCreateTokenRequest,
		encodeResponse,
	)
	router.Methods("POST").Path("/authorize/token").Handler(baseCreateTokenHandler)

	//////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
