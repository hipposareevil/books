package main

// Main application
//
// This will create the router, static files 
// and wire everything together 


import (
	"net/http"
	"os"

	// gorilla routing
	"github.com/gorilla/mux"

	"github.com/go-kit/kit/log"
	httptransport "github.com/go-kit/kit/transport/http"
)

// Main
func main() {
	logger := log.NewLogfmtLogger(os.Stderr)

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
    // 'query' service
    var querySvc QueryService
    querySvc = queryService{}
    
    ////////////////
    // Endpoints

	// GET /author

    queryAuthorEndpoint := makeQueryAuthorEndpoint(querySvc)
    queryAuthorHandler := httptransport.NewServer(
        queryAuthorEndpoint,
        decodeQueryAuthorRequest,
        encodeResponse,
    )
    // no authentication like the other services
    router.Methods("GET").Path("/query/author").Handler(queryAuthorHandler)

	// GET /title

    queryTitleEndpoint := makeQueryTitleEndpoint(querySvc)
    queryTitleHandler := httptransport.NewServer(
        queryTitleEndpoint,
        decodeQueryTitleRequest,
        encodeResponse,
    )
    // no authentication like the other services
    router.Methods("GET").Path("/query/book").Handler(queryTitleHandler)

    //////////////
	// Start server
	addr := ":8080"
	logger.Log("msg", "HTTP", "addr", addr)
	logger.Log("err", http.ListenAndServe(addr, nil))
}
