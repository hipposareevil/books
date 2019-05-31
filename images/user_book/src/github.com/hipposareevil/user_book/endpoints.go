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
/// UserBooks (all)
// response for userBooks (vs. single userBook)
type userBooksResponse struct {
	Data UserBooks `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse userBooksResponse) error() error {
	return theResponse.Err
}

func (theResponse userBooksResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// USERBOOK (single)
// response for userBook (single)
type userBookResponse struct {
	Data UserBook  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse userBookResponse) error() error {
	return theResponse.Err
}

func (theResponse userBookResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// DELETE USERBOOK (single)
// response for userBook (single)
type deleteUserBookResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse deleteUserBookResponse) error() error {
	return theResponse.Err
}

////////////////////
/// Create USERBOOK
// response for create userBook
type createUserBookResponse struct {
	Data UserBook  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse createUserBookResponse) error() error {
	return theResponse.Err
}

func (theResponse createUserBookResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// Update USERBOOK
// response for update userBook
type updateUserBookResponse struct {
	Err  error `json:"err,omitempty"`
	Data UserBook  `json:"all,omitempty"`
}

func (theResponse updateUserBookResponse) error() error {
	return theResponse.Err
}

func (theResponse updateUserBookResponse) getData() interface{} {
	return theResponse.Data
}
