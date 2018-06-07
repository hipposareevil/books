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

// GET /user/
// Make endpoint for getting users
func makeGetUsersEndpoint(svc UserService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a users specific request
		req := request.(getAllUsersRequest)

		// call actual service with data from the req
		users, err := svc.GetUsers(req.Offset, req.Limit)
		return usersResponse{
			Data: users,
			Err:  err,
		}, nil
	}
}

// GET /user/<user_id>
// Make endpoint for getting single User
func makeGetUserEndpoint(svc UserService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a userRequest
		req := request.(getUserRequest)

		// call actual service with data from the req
		user, err := svc.GetUser(req.UserId)
		return userResponse{
			Data: user,
			Err:  err,
		}, nil
	}
}

// DELETE /user/<user_id>
// Make endpoint for deleting single User
func makeDeleteUserEndpoint(svc UserService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a userRequest
		req := request.(deleteUserRequest)

		// call actual service with data from the req
		err := svc.DeleteUser(req.UserId)
		return deleteUserResponse{
			Err: err,
		}, nil
	}
}

// POST /user/
// Make endpoint for creating (via post) a user
func makeCreateUserEndpoint(svc UserService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a createUserRequest
		req := request.(createUserRequest)

		// call actual service with data from the req
		newUser, err := svc.CreateUser(req.Name,
			req.UserGroup,
			req.Data,
			req.Password)

		return createUserResponse{
			Data: newUser,
			Err:  err,
		}, nil
	}
}

// PUT /user/<user_id>
// Make endpoint for updating (via PUT) a user
func makeUpdateUserEndpoint(svc UserService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a updateUserRequest
		req := request.(updateUserRequest)

		// call actual service with data from the req (putTagRequest)
		err := svc.UpdateUser(req.Id,
			req.Name,
			req.UserGroup,
			req.Data,
			req.Password)

		return updateUserResponse{
			Err: err,
		}, nil
	}
}

//////////////////////////////////////////////////////////
//
// Decode

// Create a getAllUsersRequest from the context and http.Request
// /user/
//
// The getAllUsersRequest has 3 variables:
// - Offset   Offset into the query
// - Limit    Number of values to return
func decodeGetAllUsersRequest(_ context.Context, r *http.Request) (interface{}, error) {
	realOffset, realLimit := parseOffsetAndLimit(r)

	// Make request for all users
	var request getAllUsersRequest
	request = getAllUsersRequest{
		Offset: realOffset,
		Limit:  realLimit,
	}

	return request, nil
}

// Create getUserRequest
// /user/id
//
// The userRequest has 2 variables:
// - TagId   ID of tag taken from the path
func decodeGetUserRequest(_ context.Context, r *http.Request) (interface{}, error) {
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

	// Make request for single  user
	var request getUserRequest
	request = getUserRequest{
		UserId: userId,
	}

	return request, nil
}

// Create deleteUserRequest
// DELETE /user/id
//
// The (delete) userRequest has 2 variables:
// - TagId   ID of tag taken from the path
func decodeDeleteUserRequest(_ context.Context, r *http.Request) (interface{}, error) {
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

	// Make request to delete user
	var request deleteUserRequest
	request = deleteUserRequest{
		UserId: userId,
	}

	return request, nil
}

// Create createUserRequest
//  POST /user
func decodeCreateUserRequest(_ context.Context, r *http.Request) (interface{}, error) {
	///////////////////
	// Parse body
	var newUser createUserRequest
	if err := json.NewDecoder(r.Body).Decode(&newUser); err != nil {
		return nil, err
	}

	return newUser, nil
}

// Create updateUserRequest
//  PUT /user/id
func decodeUpdateUserRequest(_ context.Context, r *http.Request) (interface{}, error) {
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

	///////////////////
	// Parse body
	var updateUser updateUserRequest
	if err := json.NewDecoder(r.Body).Decode(&updateUser); err != nil {
		return nil, err
	}

	updateUser.Id = userId

	return updateUser, nil
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

// Decode the bearer string from the request
//
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

// Decode the 'user_id' from the request.
//
//// Returns the user id
func parseUserId(r *http.Request) (int, error) {
	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'user_id' is set in the gorilla handling in main.go
	id, ok := vars["user_id"]
	if !ok {
		return 0, ErrBadRouting
	}

	var userId int
	if id != "" {
		userId, _ = strconv.Atoi(id)
	}

	return userId, nil
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
