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
/// Authors (all)
// response for authors (vs. single author)
type authorsResponse struct {
	Data Authors `json:"all,omitempty"`
	Err  error   `json:"err,omitempty"`
}

func (theResponse authorsResponse) error() error {
	return theResponse.Err
}

func (theResponse authorsResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// AUTHOR (single)
// response for author (single)
type authorResponse struct {
	Data Author `json:"all,omitempty"`
	Err  error  `json:"err,omitempty"`
}

func (theResponse authorResponse) error() error {
	return theResponse.Err
}

func (theResponse authorResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// DELETE AUTHOR (single)
// response for author (single)
type deleteAuthorResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse deleteAuthorResponse) error() error {
	return theResponse.Err
}

////////////////////
/// Create AUTHOR
// response for create author
type createAuthorResponse struct {
	Data Author `json:"all,omitempty"`
	Err  error  `json:"err,omitempty"`
}

func (theResponse createAuthorResponse) error() error {
	return theResponse.Err
}

func (theResponse createAuthorResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// Update AUTHOR
// response for update author
type updateAuthorResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse updateAuthorResponse) error() error {
	return theResponse.Err
}
