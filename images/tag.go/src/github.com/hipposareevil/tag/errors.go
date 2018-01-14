package main

import (
	"errors"
)

// Error response as JSON with 'code' and 'message'
type ErrorResponse struct {
    Code int `json:"code"`
    Message string `json:"message"`
}

// List of common errors, used to write back HTTP error codes
var (
	ErrAlreadyExists = errors.New("already exists")
	ErrNotFound      = errors.New("not found")
	ErrUnauthorized  = errors.New("unauthorized")
	ErrBadRouting    = errors.New("inconsistent mapping between route and handler (programmer error)")
	ErrServerError   = errors.New("Server error")
)
