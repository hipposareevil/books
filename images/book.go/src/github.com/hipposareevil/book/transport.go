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

// GET /book/
// Make endpoint for getting books
func makeGetBooksEndpoint(svc BookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a books specific request
		req := request.(getAllBooksRequest)

		// call actual service with data from the req
		books, err := svc.GetBooks(
            req.Bearer,
            req.Offset,
            req.Limit,
            req.Title,
            req.AuthorId,
            req.BookId)
		return booksResponse{
			Data: books,
			Err:  err,
		}, nil
	}
}

// GET /book/<book_id>
// Make endpoint for getting single Book
func makeGetBookEndpoint(svc BookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a bookRequest
		req := request.(getBookRequest)

		// call actual service with data from the req
		book, err := svc.GetBook(req.Bearer, req.BookId)
		return bookResponse{
			Data: book,
			Err:  err,
		}, nil
	}
}

// DELETE /book/<book_id>
// Make endpoint for deleting single Book
func makeDeleteBookEndpoint(svc BookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a bookRequest
		req := request.(deleteBookRequest)

		// call actual service with data from the req
		err := svc.DeleteBook(req.BookId)
		return deleteBookResponse{
			Err: err,
		}, nil
	}
}

// POST /book/
// Make endpoint for creating (via post) a book
func makeCreateBookEndpoint(svc BookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a createBookRequest
		req := request.(createBookRequest)

		// call actual service with data from the req
		newBook, err := svc.CreateBook(
            req.Bearer,
            req.AuthorId,
            req.Description,
            req.FirstPublishedYear,
            req.GoodReadsUrl,
            req.ImageLarge,
            req.ImageMedium,
            req.ImageSmall,
            req.Isbns,
            req.OpenlibraryWorkUrl,
            req.Subjects,
            req.Title)

		return createBookResponse{
			Data: newBook,
			Err:  err,
		}, nil
	}
}

// PUT /book/<book_id>
// Make endpoint for updating (via PUT) a book
func makeUpdateBookEndpoint(svc BookService) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (interface{}, error) {
		// convert request into a updateBookRequest
		req := request.(updateBookRequest)

		// call actual service with data from the req (putBookRequest)
		book, err := svc.UpdateBook(
            req.Bearer,
            req.BookId,
            req.AuthorId,
            req.Description,
            req.FirstPublishedYear,
            req.GoodReadsUrl,
            req.ImageLarge,
            req.ImageMedium,
            req.ImageSmall,
            req.Isbns,
            req.OpenlibraryWorkUrl,
            req.Subjects,
            req.Title)

		return updateBookResponse{
            Data: book,
			Err: err,
		}, nil
	}
}

//////////////////////////////////////////////////////////
//
// Decode

// Create a getAllBooksRequest from the context and http.Request
// /book/
//
// The getAllBooksRequest has 3 variables:
// - Offset   Offset into the query
// - Limit    Number of values to return
func decodeGetAllBooksRequest(_ context.Context, r *http.Request) (interface{}, error) {
	realOffset, realLimit := parseOffsetAndLimit(r)

	///////////////////
	// Parse parameters
	r.ParseForm()
	values := r.Form

    // get Title
    title := values.Get("title")

    // get AuthorId list
    tempIds := values.Get("author_id")
    authorIds := splitCsvStringAsInts(tempIds)

    // Get BookId list
    tempIds = values.Get("book_id")
    bookIds := splitCsvStringAsInts(tempIds)

    // Get bearer from headers
    bearer := parseBearer(r)

	// Make request for all books
	var request getAllBooksRequest
	request = getAllBooksRequest{
        Bearer: bearer,
		Offset: realOffset,
		Limit:  realLimit,
        Title: title,
        AuthorId: authorIds,
        BookId: bookIds,
	}

	return request, nil
}

// Create getBookRequest
// /book/id
//
func decodeGetBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
    // Get book ID from gorilla handling of vars
	bookId, err := parseBookId(r)
	if err != nil {
		return nil, err
	}

    // Get bearer from headers
    bearer := parseBearer(r)

	// Make request for single  book
	var request getBookRequest
	request = getBookRequest{
		BookId: bookId,
        Bearer: bearer,
	}

	return request, nil
}

// Create deleteBookRequest
// DELETE /book/id
//
// The (delete) bookRequest has 2 variables:
// - BookId   ID of book taken from the path
func decodeDeleteBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
	bookId, err := parseBookId(r)
	if err != nil {
		return nil, err
	}

	// Make request to delete book
	var request deleteBookRequest
	request = deleteBookRequest{
		BookId: bookId,
	}

	return request, nil
}

// Create createBookRequest
//  POST /book
func decodeCreateBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
    // Get bearer from headers
    bearer := parseBearer(r)

	///////////////////
	// Parse body
	var createBookRequest createBookRequest
	if err := json.NewDecoder(r.Body).Decode(&createBookRequest); err != nil {
		return nil, err
	}

    createBookRequest.Bearer = bearer


	return createBookRequest, nil
}

// Create updateBookRequest
//  PUT /book/id
func decodeUpdateBookRequest(_ context.Context, r *http.Request) (interface{}, error) {
	bookId, err := parseBookId(r)
	if err != nil {
		return nil, err
	}

    // Get bearer from headers
    bearer := parseBearer(r)

	///////////////////
	// Parse body
	var updateBook updateBookRequest
	if err := json.NewDecoder(r.Body).Decode(&updateBook); err != nil {
		return nil, err
	}

    // Set bookid on update request
	updateBook.BookId = bookId

    updateBook.Bearer = bearer

	return updateBook, nil
}

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

////////////
// Split a CSV string into array of ints
func splitCsvStringAsInts(csv string) []int {
    var newArray []int

    fmt.Println("Converting csv string:"+csv+": into array of ints")

    if len(csv) > 0 {
        stringArray  := strings.Split(csv, ",")

        fmt.Println("new string array>", stringArray)
        fmt.Println("new string array>", len(stringArray))

        
        // Convert each string to int
        for _, element := range stringArray {
            temp, _ := strconv.Atoi(element)
            fmt.Println("Got element: ", element)
            newArray = append(newArray, temp)
        }
    }

    fmt.Println("returning newarray:",newArray)
    
	return newArray
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
