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
// Responses are passed to 'transport.encodeResponse'

////////////////////
/// Authors
// response for query author
type authorsResponse struct {
	Data Authors  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

// authorsResponse.error
func (theResponse authorsResponse) error() error {
	return theResponse.Err
}

// authorsResponse.getData
func (theResponse authorsResponse) getData() interface{} {
	return theResponse.Data
}


////////////////////
/// Titles
// response for query title
type titlesResponse struct {
	Data Titles  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

// titlesResponse.error
func (theResponse titlesResponse) error() error {
	return theResponse.Err
}

// titlesResponse.getData
func (theResponse titlesResponse) getData() interface{} {
	return theResponse.Data
}

