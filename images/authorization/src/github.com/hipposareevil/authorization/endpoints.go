package main

// Interface denoting a Response can contain an error
type errorer interface {
	error() error
}

// Interface denoting a Response can contain data
type dataHolder interface {
	// Get the data
	getData() interface{}
}

////////////////
// Responses are passed to 'transport.encodeResponse'

// Authorization Response
type authorizationResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse authorizationResponse) error() error {
	return theResponse.Err
}

// Create Token Response
type createTokenResponse struct {
	Data Authorization `json:"all,omitempty"`
	Err  error         `json:"err,omitempty"`
}

func (theResponse createTokenResponse) getData() interface{} {
	return theResponse.Data
}

func (theResponse createTokenResponse) error() error {
	return theResponse.Err
}
