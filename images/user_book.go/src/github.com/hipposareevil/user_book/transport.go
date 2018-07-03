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

// GET /user_book/<user_id>
// Make endpoint for getting books
func makeGetUserBooksEndpoint(svc UserBookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a books specific request
		req := request.(getAllUserBooksRequest)

		// call actual service with data from the req
		userBooks, err := svc.GetUserBooks(
            req.Bearer,
            req.UserId,
            req.Offset,
            req.Limit,
            req.BookId,
            req.Title,
            req.Tag)

		return userBooksResponse{
			Data: userBooks,
			Err:  err,
		}, nil
	}
}

// GET /book/<book_id>
// Make endpoint for getting single Book
func makeGetUserBookEndpoint(svc UserBookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a bookRequest
		req := request.(getUserBookRequest)

		// call actual service with data from the req
		book, err := svc.GetUserBook(
            req.Bearer,
            req.UserId,
            req.UserBookId)

		return userBookResponse{
			Data: book,
			Err:  err,
		}, nil
	}
}

// DELETE /book/<book_id>
// Make endpoint for deleting single Book
func makeDeleteUserBookEndpoint(svc UserBookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a bookRequest
		req := request.(deleteUserBookRequest)

		// call actual service with data from the req
		err := svc.DeleteUserBook(
            req.UserId,
            req.UserBookId)
		return deleteUserBookResponse{
			Err: err,
		}, nil
	}
}

// POST /book/
// Make endpoint for creating (via post) a book
func makeCreateUserBookEndpoint(svc UserBookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a createBookRequest
		req := request.(createUserBookRequest)

		// call actual service with data from the req
		newBook, err := svc.CreateUserBook(
            req.Bearer,
            req.UserId,
            req.BookId,
            req.Rating,
            req.Tags)

		return createUserBookResponse{
			Data: newBook,
			Err:  err,
		}, nil
	}
}

// PUT /book/<book_id>
// Make endpoint for updating (via PUT) a book
func makeUpdateUserBookEndpoint(svc UserBookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a updateBookRequest
		req := request.(updateUserBookRequest)

		// call actual service with data from the req (putBookRequest)
		book, err := svc.UpdateUserBook(
            req.Bearer,
            req.UserId,
            req.UserBookId,
            req.BookId,
            req.Rating,
            req.Tags)


		return updateUserBookResponse{
			Data: book,
			Err:  err,
		}, nil
	}
}

//////////////////////////////////////////////////////////
//
// Decode

// Create a getAllUserBooksRequest from the context and http.Request
// /user_book/<user_id>
//
func decodeGetAllUserBooksRequest(_ context.Context, r *http.Request) (interface{}, error) {
    // Get offset, limit and bearer
	realOffset, realLimit := parseOffsetAndLimit(r)
	bearer := parseBearer(r)

    // Get userId
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

	///////////////////
	// Parse parameters
	r.ParseForm()
	values := r.Form

    temp := values["tag"]
    tagsString := strings.Join(temp, ",")
	tags := splitCsvStringToArray(tagsString)

    // get book_id
	tempId := values.Get("book_id")
	bookId, _ := strconv.Atoi(tempId)

    // get title
	title := values.Get("title")

	// Make request for all books
	var request getAllUserBooksRequest
	request = getAllUserBooksRequest {
		Bearer:   bearer,
		Offset:   realOffset,
		Limit:    realLimit,

        UserId:   userId,
		Title:    title,
		BookId:   bookId,
        Tag:      tags,
	}

	return request, nil
}

// Create getUserBookRequest
// /user_book/<user_id>/<user_book_id>
//
func decodeGetUserBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
	bearer := parseBearer(r)

    // Get userId
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

    // Get user_book_id
	userBookId, err := parseUserBookId(r)
	if err != nil {
		return nil, err
	}

	// Make request for all books
	var request getUserBookRequest
	request = getUserBookRequest {
		Bearer:   bearer,
        UserId:   userId,
        UserBookId:   userBookId,
	}

	return request, nil

}

// Create deleteUserBookRequest
// DELETE /user_book/<user_id>/<user_book_id>
//
func decodeDeleteUserBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
    // Get userId
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

    // Get user_book_id
	userBookId, err := parseUserBookId(r)
	if err != nil {
		return nil, err
	}

	// Make request for all books
	var request deleteUserBookRequest
	request = deleteUserBookRequest {
        UserId:   userId,
        UserBookId:   userBookId,
	}

	return request, nil

}

// Create creatUsereBookRequest
//  POST /user_book/<user_id>
func decodeCreateUserBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
    // Get userId
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

	// Get bearer from headers
	bearer := parseBearer(r)

	///////////////////
	// Parse body
	var request createUserBookRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		return nil, err
	}

	// Set rest on update request
	request.UserId = userId
    request.Bearer = bearer

	return request, nil
}

// Create updateBookRequest
//  PUT /user_book/<user_id>/<user_book_id>
func decodeUpdateUserBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
    // Get userId
	userId, err := parseUserId(r)
	if err != nil {
		return nil, err
	}

    // Get user_book_id
	userBookId, err := parseUserBookId(r)
	if err != nil {
		return nil, err
	}

	// Get bearer from headers
	bearer := parseBearer(r)

	///////////////////
	// Parse body
	var request updateUserBookRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		return nil, err
	}

	// Set rest on update request
	request.UserId = userId
	request.UserBookId = userBookId
    request.Bearer = bearer

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
		// default to get 30
		realLimit = 30
	}

	return realOffset, realLimit
}

// Decode the 'user_book_id' from the request.
//
//// Returns the user book id
func parseUserBookId(r *http.Request) (int, error) {
	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'user_book_id' is set in the gorilla handling in main.go
	id, ok := vars["user_book_id"]
	if !ok {
		return 0, ErrBadRouting
	}

	var userBookId int
	if id != "" {
		userBookId, _ = strconv.Atoi(id)
	}

	return userBookId, nil
}

// Decode the 'user_id' from the request.
//
//// Returns the user id
func parseUserId(r *http.Request) (int, error) {
	// Demux the gorilla parsing
	vars := mux.Vars(r)
	// 'user_book_id' is set in the gorilla handling in main.go
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
