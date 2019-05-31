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
/// TAGS
// response for tags (vs. single tag)
type tagsResponse struct {
	Data Tags  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

// tagsResponse.error
func (theResponse tagsResponse) error() error {
	return theResponse.Err
}

// tagsResponse.getData
func (theResponse tagsResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// TAG (single)
// response for tag (single)
type tagResponse struct {
	Data Tag   `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

// tagResponse.error
func (theResponse tagResponse) error() error {
	return theResponse.Err
}

// tagResponse.getData
func (theResponse tagResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// PUT TAG
type putTagResponse struct {
	Err error `json:"err,omitempty"`
}

// deleteTagResponse.error
func (theResponse putTagResponse) error() error {
	return theResponse.Err
}


////////////////////
/// DELETE TAG (single)
// response for tag (single)
type deleteTagResponse struct {
	Err error `json:"err,omitempty"`
}

// deleteTagResponse.error
func (theResponse deleteTagResponse) error() error {
	return theResponse.Err
}
