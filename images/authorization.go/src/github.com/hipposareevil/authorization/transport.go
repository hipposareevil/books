package main

// Transport module
// 
// Contains:
// - endpoint creation
// - encode responses to client
// - decode client requests

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	_ "strconv"
	"strings"

	"github.com/go-kit/kit/endpoint"
)

//////////////////////////////////////////////////////////
//
// Create endpoints

// Get /authorize/validate
// Make endpoint for validating the authorization header
func makeGetValidationEndpoint(svc AuthorizeService) endpoint.Endpoint {
    return func(ctx context.Context, request interface{}) (interface{}, error) {
        // convert request into an authorizationRequest
		req := request.(authorizationRequest)

		// call actual service with data from the req
		err := svc.ValidateToken(req.Bearer)
		return authorizationResponse {
			Err:  err,
		}, nil
    }
}

// POST /authorize/token
// Make endpoint to create token
func makeCreateTokenEndpoint(svc AuthorizeService) endpoint.Endpoint {
    return func(ctx context.Context, request interface{}) (interface{}, error) {
        // convert request into an createTokenRequest
		req := request.(createTokenRequest)

		// call actual service with data from the req
		authorization, err := svc.CreateToken(req.Name, req.Password)
		return createTokenResponse  {
            Data: authorization,
			Err:  err,
		}, nil
    }
}


//////////////////////////////////////////////////////////
//
// Decode from client


// Create an authorizationRequest
// (used by service.ValidateToken)
// /authorize/validate
//
// The request has 1 variable:
// - Bearer  Authorization token
func decodeValidationRequest(_ context.Context, r *http.Request) (interface{}, error) {
	///////////////////
	// parse headers
	var realBearer string
	bearer := r.Header.Get("authorization")

	// Strip the 'Bearer ' from header
	if strings.HasPrefix(bearer, "Bearer ") {
		realBearer = strings.Replace(bearer, "Bearer ", "", 1)
	} 

	// Make request for authorization
	var request authorizationRequest
	request = authorizationRequest{
		Bearer: realBearer,
	}

	return request, nil
}


// Create a createTokenRequest
// (used by service.CreateToken)
//
// The request has 2 variables:
// - User
// - Password
func decodeCreateTokenRequest(_ context.Context, r *http.Request) (interface{}, error) {
    ///////////////////
    // Parse body

    // Decode the incoming JSON into a Credentials struct
    var credentials Credentials
    if err := json.NewDecoder(r.Body).Decode(&credentials); err != nil {
        return nil, err
    }

    // Make request
    var request createTokenRequest
    request = createTokenRequest{
        credentials.Name,
        credentials.Password,
    }

	return request, nil
}


//////////////////////////////////////////////////////////
//
//  Encode responses to client

// The response should be of type errorer and thus can be cast to check if there is an error
func encodeResponse(ctx context.Context, w http.ResponseWriter, response interface{}) error {
	// Set JSON type
	w.Header().Set("Content-Type", "application/json; charset=utf-8")

	// Check error.
    // All Response objects in endpoints.go should implement "error()" so
    // we can see if there was a proper error to handle
	if e, ok := response.(errorer); ok {
		// This is a errorer class, now check for error
		if err := e.error(); err != nil {
            fmt.Println(e.error())
			encodeError(ctx, e.error(), w)
			return nil
		}
	}

    // Write out normal response. See if the response is a DataHolder
	// Cast to dataHolder to get Data, otherwise just encode the resposne
	if holder, ok := response.(dataHolder); ok {
        // This is a 'dataHolder'
        fmt.Println("dataholder response")
		return json.NewEncoder(w).Encode(holder.getData())
	} else {
        fmt.Println("normal response")
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

	// write out the error message
	json.NewEncoder(w).Encode(map[string]interface{}{
        "code": code,
		"message": err.Error(),
	})
}

// Determine the HTTP error code from the incoming error 'err'
func codeFrom(err error) int {
	switch err {
	case ErrUnauthorized:
		return http.StatusUnauthorized
    case ErrNotFound:
        return http.StatusNotFound
	default:
		return http.StatusInternalServerError
	}
}
