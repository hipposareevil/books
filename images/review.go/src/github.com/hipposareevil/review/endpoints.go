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
/// Reviews (all)
// response for reviews
type reviewResponse struct {
	Data Reviews `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse reviewResponse) error() error {
	return theResponse.Err
}

func (theResponse reviewResponse) getData() interface{} {
	return theResponse.Data
}

