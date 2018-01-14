package main

// Transport module
// 
// Contains:
// - endpoint creation
// - encode responses to client
// - decode client requests
// - structures used. e.g. tagRequest, postTagRequest, etc

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

// GET /tag/
// Make endpoint for getting TAGS
func makeTagsEndpoint(svc TagService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a tagsRequest
		req := request.(tagsRequest)

		// call actual service with data from the req (tagsRequest)
		tags, err := svc.GetTags(req.Bearer, req.Offset, req.Limit)
		return tagsResponse{
			Data: tags,
			Err:  err,
		}, nil
	}
}

// POST /tag/
// Make endpoint for creating (via post) a tag
func makePostTagEndpoint(svc TagService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a tagsRequest
		req := request.(postTagRequest)

		// call actual service with data from the req (postTagRequest)
		tag, err := svc.CreateTag(req.Bearer, req.NewTagName)
		return tagResponse{
			Data: tag,
			Err:  err,
		}, nil
	}
}


// PUT /tag/
// Make endpoint for updating (via put) a tag
func makePutTagEndpoint(svc TagService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a putTagRequest
		req := request.(putTagRequest)

		// call actual service with data from the req (putTagRequest)
		err := svc.UpdateTag(req.Bearer, req.NewTagName, req.TagId)
		return putTagResponse{
			Err:  err,
		}, nil
	}
}


// GET /tag/<tag_id>
// Make endpoint for getting single TAG
func makeTagEndpoint(svc TagService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a tagRequest
		req := request.(tagRequest)

		// call actual service with data from the req (tagsRequest)
		tag, err := svc.GetTag(req.Bearer, req.TagId)
		return tagResponse{
			Data: tag,
			Err:  err,
		}, nil
	}
}

// DELETE /tag/<tag_id>
// Make endpoint for deleting single TAG
func makeDeleteTagEndpoint(svc TagService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a tagRequest
		// This re-uses the normal tagRequest as the parameters are identical
		req := request.(tagRequest)

		// call actual service with data from the req (tagsRequest)
		err := svc.DeleteTag(req.Bearer, req.TagId)
		return deleteTagResponse{
			Err: err,
		}, nil
	}
}

//////////////////////////////////////////////////////////
//
// Structures


// GET request for single tag, contains:
// - tag_id
// - bearer
type tagRequest struct {
	TagId  int    `json:"tag_id"`
	Bearer string `json:"bearer"`
}


// DELETE request for single tag, contains:
// - tag_id
// - bearer
type deleteTagRequest struct {
	TagId  int    `json:"tag_id"`
	Bearer string `json:"bearer"`
}


// GET request for tags, contains:
// - offset
// - limit
// - bearer
type tagsRequest struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Bearer string `json:"bearer"`
}

// This contains header information like:
// - Bearer
// - New tag name
type postTagRequest struct {
	Bearer     string `json:"bearer"`
	NewTagName string `json:"String"`
}

// PUT request, contains:
// - Bearer
// - Updated tag name
// - tag id
type putTagRequest struct {
	TagId  int    `json:"tag_id"`
	Bearer     string `json:"bearer"`
	NewTagName string `json:"String"`
}



//////////////////////////////////////////////////////////
//
// Decode


// Create a tagRequest
// (used by getTag and deleteTag
// /TAG/<tag_id>
//
// The tagRequest has 2 variables:
// - TagId   ID of tag taken from the path
// - Bearer  Authorization token
func decodeTagRequest(_ context.Context, r *http.Request) (interface{}, error) {
    ///////////////////
    // Parse the URL

	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'tag_id' is set in the gorilla handling in main.go
	id, ok := vars["tag_id"]
	if !ok {
		return nil, ErrBadRouting
	}

	var tagId int
	if id != "" {
		tagId, _ = strconv.Atoi(id)
	}

	///////////////////
	// parse headers
	var realBearer string
	bearer := r.Header.Get("authorization")

	// Strip the 'Bearer ' from header
	if strings.HasPrefix(bearer, "Bearer ") {
		realBearer = strings.Replace(bearer, "Bearer ", "", 1)
	}

	// Make request for single tag
	var request tagRequest
	request = tagRequest{
		Bearer: realBearer,
		TagId:  tagId,
	}

	return request, nil
}

// Create a postTagRequest
// /TAG
//
// The postTagRequest has 2 variables:
// - Name    Name of tag to create
// - Bearer  Authorization token
func decodePostTagRequest(_ context.Context, r *http.Request) (interface{}, error) {
    ///////////////////
    // Parse body

	// Decode the incoming JSON into a NewTag struct

	var newTag NewTag
	if err := json.NewDecoder(r.Body).Decode(&newTag); err != nil {
		return nil, err
	}

	// Got the newtag info
	fmt.Println("Got new tag name: ", newTag.Name)

	///////////////////
	// Parse headers
	var realBearer string
	bearer := r.Header.Get("authorization")

	// Strip the 'Bearer ' from header
	if strings.HasPrefix(bearer, "Bearer ") {
		realBearer = strings.Replace(bearer, "Bearer ", "", 1)
	}

	// Make request for tags
	var request postTagRequest
	request = postTagRequest{
		Bearer:     realBearer,
		NewTagName: newTag.Name,
	}

	return request, nil
}


// Create a PUT tagRequest
// /TAG/<tag_id>
//
// The tagRequest has 2 variables:
// - TagId   ID of tag taken from the path
// - Bearer  Authorization token
// - Name    Name of tag to update
func decodePutTagRequest(_ context.Context, r *http.Request) (interface{}, error) {
    ///////////////////
    // Parse the URL

	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'tag_id' is set in the gorilla handling in main.go
	id, ok := vars["tag_id"]
	if !ok {
		return nil, ErrBadRouting
	}

    // Get tag id
	var tagId int
	if id != "" {
		tagId, _ = strconv.Atoi(id)
	}

    ///////////////////
    // Parse body
	var updatedTag NewTag
	if err := json.NewDecoder(r.Body).Decode(&updatedTag); err != nil {
		return nil, err
	}

	// Got the newtag info
	fmt.Println("Got new updated name: ", updatedTag.Name)

	///////////////////
	// parse headers
	var realBearer string
	bearer := r.Header.Get("authorization")

	// Strip the 'Bearer ' from header
	if strings.HasPrefix(bearer, "Bearer ") {
		realBearer = strings.Replace(bearer, "Bearer ", "", 1)
	}

	// Make request for single tag
	var request putTagRequest
	request = putTagRequest{
		Bearer: realBearer,
		TagId:  tagId,
        NewTagName: updatedTag.Name,
	}

	return request, nil
}


// Create a tagsRequest from the context and http.Request
// /TAG/
//
// The tagsRequest has 3 variables:
// - Offset   Offset into the query
// - Limit    Number of values to return
// - Bearer  Authorization token
func decodeTagsRequest(_ context.Context, r *http.Request) (interface{}, error) {
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

	///////////////////
	// parse headers
	var realBearer string
	bearer := r.Header.Get("authorization")

	// Strip the 'Bearer ' from header
	if strings.HasPrefix(bearer, "Bearer ") {
		realBearer = strings.Replace(bearer, "Bearer ", "", 1)
	}

	// Make request for tags
	var request tagsRequest
	request = tagsRequest{
		Offset: realOffset,
		Limit:  realLimit,
		Bearer: realBearer,
	}

	return request, nil
}

//////////////////////////////////////////////////////////
//
//  Encode responses to client

// The response can/should be of type errorer and thus can be cast to check if there is an error
func encodeResponse(ctx context.Context, w http.ResponseWriter, response interface{}) error {
	fmt.Println("encoding tag response.")
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
	w.WriteHeader(codeFrom(err))

	// write out the error message
	json.NewEncoder(w).Encode(map[string]interface{}{
		"error": err.Error(),
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
