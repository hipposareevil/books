package main

// Transport module
// 
// Contains:
// - endpoint creation
// - encode responses to client
// - decode client requests

import (
	"context"
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/go-kit/kit/endpoint"
)

//////////////////////////////////////////////////////////
//
// Create endpoints


// GET /query/author
func makeQueryAuthorEndpoint(svc QueryService) endpoint.Endpoint {

	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a queryAuthorRequest
		req := request.(queryAuthorRequest)

		// call actual service with data from the request
        authors, err := svc.QueryAuthor(req.Author, req.Offset, req.Limit)
		return authorsResponse{
			Data: authors,
			Err:  err,
		}, nil
	}
}


// GET /query/title
func makeQueryTitleEndpoint(svc QueryService) endpoint.Endpoint {

	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a queryTitleRequest
		req := request.(queryTitleRequest)

		// call actual service with data from the request
        titles, err := svc.QueryTitle(req.Author,
            req.Title,
            req.Isbn,
            req.Offset, req.Limit)
		return titlesResponse{
			Data: titles,
			Err:  err,
		}, nil
	}
}


//////////////////////////////////////////////////////////
//
// Decode


// Create a queryAuthorRequest
func decodeQueryAuthorRequest(_ context.Context, r *http.Request) (interface{}, error) {
	realOffset, realLimit := parseOffsetAndLimit(r)

    // Get author name
	r.ParseForm()
	values := r.Form

	authorName := values.Get("author")

	var request queryAuthorRequest
	request = queryAuthorRequest {
		Offset: realOffset,
		Limit:  realLimit,
        Author: authorName,
	}

	return request, nil
}



// Create a queryTitleRequest
func decodeQueryTitleRequest(_ context.Context, r *http.Request) (interface{}, error) {
	realOffset, realLimit := parseOffsetAndLimit(r)

    // Get params
	r.ParseForm()
	values := r.Form

	authorName := values.Get("author")
	title := values.Get("title")
	isbn := values.Get("isbn")

	var request queryTitleRequest
	request = queryTitleRequest {
		Offset: realOffset,
		Limit:  realLimit,
        Author: authorName,
        Isbn: isbn,
        Title: title,
	}

	return request, nil
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

	// write out the error message
	json.NewEncoder(w).Encode(map[string]interface{}{
        "code": code,
		"message": err.Error(),
	})
}

// Determine the HTTP error code from the incoming error 'err'
func codeFrom(err error) int {
	switch err {
	case ErrUnauthorized:
		return http.StatusUnauthorized
	default:
		return http.StatusInternalServerError
	}
}



