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
/// Users (all)
// response for users (vs. single user)
type usersResponse struct {
	Data Users `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse usersResponse) error() error {
	return theResponse.Err
}

func (theResponse usersResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// USER (single)
// response for user (single)
type userResponse struct {
	Data User  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse userResponse) error() error {
	return theResponse.Err
}

func (theResponse userResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// DELETE USER (single)
// response for user (single)
type deleteUserResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse deleteUserResponse) error() error {
	return theResponse.Err
}

////////////////////
/// Create USER
// response for create user
type createUserResponse struct {
	Data User  `json:"all,omitempty"`
	Err  error `json:"err,omitempty"`
}

func (theResponse createUserResponse) error() error {
	return theResponse.Err
}

func (theResponse createUserResponse) getData() interface{} {
	return theResponse.Data
}

////////////////////
/// Update USER
// response for update user
type updateUserResponse struct {
	Err error `json:"err,omitempty"`
}

func (theResponse updateUserResponse) error() error {
	return theResponse.Err
}
