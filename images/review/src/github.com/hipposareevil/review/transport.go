package main

// Transport module
//
// Contains:
// - endpoint creation
// - encode responses to client
// - decode client requests
// - structures used. e.g. bookRequest, postBookRequest, etc

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"github.com/gorilla/mux"

	"github.com/go-kit/kit/endpoint"
)

//////////////////////////////////////////////////////////
//
// Create endpoints

// GET /review/<book_id>
// Make endpoint for getting reviews
func makeGetReviewsEndpoint(svc ReviewService) endpoint.Endpoint {

	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a reviews specific request
		req := request.(getReviewsRequest)

		// call actual service with data from the req
		reviews, err := svc.GetReviews(
			req.Bearer,
			req.Offset,
			req.Limit,
			req.BookId)

		return reviewResponse{
			Data: reviews,
			Err:  err,
		}, nil
	}
}

//////////////////////////////////////////////////////////
//
// Decode

// Create a getReviewRequestfrom the context and http.Request
// /review/<book_id>
//
func decodeGetReviewsRequest(_ context.Context, r *http.Request) (interface{}, error) {
	// Get offset, limit and bearer
	realOffset, realLimit := parseOffsetAndLimit(r)
	bearer := parseBearer(r)

	// Get book id
	bookId, err := parseBookId(r)
	if err != nil {
		return nil, err
	}

	// Make request for all books
	var request getReviewsRequest
	request = getReviewsRequest{
		Bearer: bearer,
		Offset: realOffset,
		Limit:  realLimit,
		BookId: bookId,
	}

	return request, nil
}

/////////////////////////////
// Helper methods

// Returns the bearer id without "Bearer "
func parseBearer(r *http.Request) string {
	var realBearer string
	bearer := r.Header.Get("authorization")

	// Strip the 'Bearer ' from header
	if strings.HasPrefix(bearer, "Bearer ") {
		realBearer = strings.Replace(bearer, "Bearer ", "", 1)
	}

	return realBearer
}

// Decode the common parts of a request:
// * offset
// * limit
//
// Instead of erroring out, it will return defaults
//
// Returns the two values in order: offset & limit
func parseOffsetAndLimit(r *http.Request) (int, int) {
	///////////////////
	// Parse parameters
	r.ParseForm()
	values := r.Form

	// Get values from the form, where 'offset' & 'limit' are parameters
	var realOffset int
	var realLimit int

	// Offset, use a default of 0
	offset := values.Get("offset")
	if offset != "" {
		realOffset, _ = strconv.Atoi(offset)
	} else {
		realOffset = 0
	}

	// Limit, set a default if it doesn't exist
	limit := values.Get("limit")
	if limit != "" {
		realLimit, _ = strconv.Atoi(limit)
	} else {
		// default to get 20
		realLimit = 20
	}

	return realOffset, realLimit
}

// Decode the 'book_id' from the request.
//
//// Returns the book id
func parseBookId(r *http.Request) (int, error) {
	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'book_id' is set in the gorilla handling in main.go
	id, ok := vars["book_id"]
	if !ok {
		return 0, ErrBadRouting
	}

	var bookId int
	if id != "" {
		bookId, _ = strconv.Atoi(id)
	}

	return bookId, nil
}

//////////////////////////////////////////////////////////
//
//  Encode responses to client

// The response can/should be of type errorer and thus can be cast to check if there is an error
func encodeResponse(ctx context.Context, w http.ResponseWriter, response interface{}) error {
	// Set JSON type
	w.Header().Set("Content-Type", "application/json; charset=utf-8")

	// Check error
	if e, ok := response.(errorer); ok {
		// This is a errorer class, now check for error
		if err := e.error(); err != nil {
			encodeError(ctx, e.error(), w)
			return nil
		}
	}

	// cast to dataHolder to get Data, otherwise just encode the resposne
	if holder, ok := response.(dataHolder); ok {
		return json.NewEncoder(w).Encode(holder.getData())
	} else {
		return json.NewEncoder(w).Encode(response)
	}
}

// Write the incoming err into the response writer
func encodeError(_ context.Context, err error, w http.ResponseWriter) {
	if err == nil {
		panic("encodeError with nil error")
	}

	w.Header().Set("Content-Type", "application/json; charset=utf-8")

	// Write actual error code
	code := codeFrom(err)
	w.WriteHeader(code)

	fmt.Println("Sending back error '" + err.Error() + "'")

	// write out the error message
	json.NewEncoder(w).Encode(map[string]interface{}{
		"code":    code,
		"message": err.Error(),
	})
}

// Determine the HTTP error code from the incoming error 'err'
func codeFrom(err error) int {
	switch err {
	case ErrNotFound:
		return http.StatusNotFound
	case ErrAlreadyExists:
		return http.StatusConflict
	case ErrUnauthorized:
		return http.StatusUnauthorized
	default:
		return http.StatusInternalServerError
	}
}
