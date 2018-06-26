package main

// Transport module
//
// Contains:
// - endpoint creation
// - encode responses to client
// - decode client requests
// - structures used. e.g. authorRequest, postAuthorRequest, etc

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"

	"github.com/go-kit/kit/endpoint"
)

//////////////////////////////////////////////////////////
//
// Create endpoints

// GET /author/
// Make endpoint for getting authors
func makeGetAuthorsEndpoint(svc AuthorService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a authors specific request
		req := request.(getAllAuthorsRequest)

		// call actual service with data from the req
		authors, err := svc.GetAuthors(req.Offset, req.Limit)
		return authorsResponse{
			Data: authors,
			Err:  err,
		}, nil
	}
}

// GET /author/<author_id>
// Make endpoint for getting single Author
func makeGetAuthorEndpoint(svc AuthorService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a authorRequest
		req := request.(getAuthorRequest)

		// call actual service with data from the req
		author, err := svc.GetAuthor(req.AuthorId)
		return authorResponse{
			Data: author,
			Err:  err,
		}, nil
	}
}

// DELETE /author/<author_id>
// Make endpoint for deleting single Author
func makeDeleteAuthorEndpoint(svc AuthorService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a authorRequest
		req := request.(deleteAuthorRequest)

		// call actual service with data from the req
		err := svc.DeleteAuthor(req.AuthorId)
		return deleteAuthorResponse{
			Err: err,
		}, nil
	}
}

// POST /author/
// Make endpoint for creating (via post) a author
func makeCreateAuthorEndpoint(svc AuthorService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a createAuthorRequest
		req := request.(createAuthorRequest)

		// call actual service with data from the req
		newAuthor, err := svc.CreateAuthor(req.Name,
			req.BirthDate,
			req.OlKey,
			req.GoodReadsUrl,
			req.ImageSmall,
			req.ImageMedium,
			req.ImageLarge,
			req.Subjects)

		return createAuthorResponse{
			Data: newAuthor,
			Err:  err,
		}, nil
	}
}

// PUT /author/<author_id>
// Make endpoint for updating (via PUT) a author
func makeUpdateAuthorEndpoint(svc AuthorService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a updateAuthorRequest
		req := request.(updateAuthorRequest)

		// call actual service with data from the req (putAuthorRequest)
		err := svc.UpdateAuthor(req.Id,
			req.Name,
			req.BirthDate,
			req.OlKey,
			req.GoodReadsUrl,
			req.ImageSmall,
			req.ImageMedium,
			req.ImageLarge,
			req.Subjects)

		return updateAuthorResponse{
			Err: err,
		}, nil
	}
}

//////////////////////////////////////////////////////////
//
// Decode

// Create a getAllAuthorsRequest from the context and http.Request
// /author/
//
// The getAllAuthorsRequest has 3 variables:
// - Offset   Offset into the query
// - Limit    Number of values to return
func decodeGetAllAuthorsRequest(_ context.Context, r *http.Request) (interface{}, error) {
	realOffset, realLimit := parseOffsetAndLimit(r)

	// Make request for all authors
	var request getAllAuthorsRequest
	request = getAllAuthorsRequest{
		Offset: realOffset,
		Limit:  realLimit,
	}

	return request, nil
}

// Create getAuthorRequest
// /author/id
//
func decodeGetAuthorRequest(_ context.Context, r *http.Request) (interface{}, error) {
	authorId, err := parseAuthorId(r)
	if err != nil {
		return nil, err
	}

	// Make request for single  author
	var request getAuthorRequest
	request = getAuthorRequest{
		AuthorId: authorId,
	}

	return request, nil
}

// Create deleteAuthorRequest
// DELETE /author/id
//
// The (delete) authorRequest has 2 variables:
// - AuthorId   ID of author taken from the path
func decodeDeleteAuthorRequest(_ context.Context, r *http.Request) (interface{}, error) {
	authorId, err := parseAuthorId(r)
	if err != nil {
		return nil, err
	}

	// Make request to delete author
	var request deleteAuthorRequest
	request = deleteAuthorRequest{
		AuthorId: authorId,
	}

	return request, nil
}

// Create createAuthorRequest
//  POST /author
func decodeCreateAuthorRequest(_ context.Context, r *http.Request) (interface{}, error) {
	///////////////////
	// Parse body
	var newAuthor createAuthorRequest
	if err := json.NewDecoder(r.Body).Decode(&newAuthor); err != nil {
		return nil, err
	}

	return newAuthor, nil
}

// Create updateAuthorRequest
//  PUT /author/id
func decodeUpdateAuthorRequest(_ context.Context, r *http.Request) (interface{}, error) {
	authorId, err := parseAuthorId(r)
	if err != nil {
		return nil, err
	}

	///////////////////
	// Parse body
	var updateAuthor updateAuthorRequest
	if err := json.NewDecoder(r.Body).Decode(&updateAuthor); err != nil {
		return nil, err
	}

	updateAuthor.Id = authorId

	return updateAuthor, nil
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
		// default to get 30
		realLimit = 30
	}

	return realOffset, realLimit
}

// Decode the 'author_id' from the request.
//
//// Returns the author id
func parseAuthorId(r *http.Request) (int, error) {
	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'author_id' is set in the gorilla handling in main.go
	id, ok := vars["author_id"]
	if !ok {
		return 0, ErrBadRouting
	}

	var authorId int
	if id != "" {
		authorId, _ = strconv.Atoi(id)
	}

	return authorId, nil
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
