package main

// Base for all responses
type errorer interface {
	error() error
}

// interface for holding data
type dataHolder interface {
	// Get the data
	getData() interface{}
}

////////////////
// Responses are  passed to 'transport.encodeResponse'

////////////////////
/// Books (all)
// response for books (vs. single book)
type booksResponse struct {
	Data Books `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse booksResponse) error() error {
	return theResponse.Err
}

func (theResponse booksResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// BOOK (single)
// response for book (single)
type bookResponse struct {
	Data Book  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse bookResponse) error() error {
	return theResponse.Err
}

func (theResponse bookResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// DELETE BOOK (single)
// response for book (single)
type deleteBookResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse deleteBookResponse) error() error {
	return theResponse.Err
}

////////////////////
/// Create BOOK
// response for create book
type createBookResponse struct {
	Data Book  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse createBookResponse) error() error {
	return theResponse.Err
}

func (theResponse createBookResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// Update BOOK
// response for update book
type updateBookResponse struct {
	Err  error `json:"err,omitempty"`
	Data Book  `json:"all,omitempty"`
}

func (theResponse updateBookResponse) error() error {
	return theResponse.Err
}

func (theResponse updateBookResponse) getData() interface{} {
	return theResponse.Data
}
