package main

///////////////////////
// Structures
///////////////////////

////////////////
// DB structure
type User struct {
	UserId    int
	Name      string
	UserGroup string
	Password  string
}

////////////////
// Structures used to send data from client to our server

// Values used to create a token
// Used in POST
type Credentials struct {
	Name     string `json:"name"`
	Password string `json:"password"`
}

////////////////
// Structures sent to client

// Authorization JSON structure sent back to client
type Authorization struct {
	Token     string `json:"token"`
	UserId    int    `json:"userId"`
	GroupName string `json:"groupName"`
}

/////////////
// Requests used internally
// They are created by transport.decode and passed along to transport.makeXXXEndpoint

// GET request to verify header is OK
type authorizationRequest struct {
	Bearer string `json:"bearer"`
}

// POST request to create token
type createTokenRequest struct {
	Name     string `json:"name"`
	Password string `json:"password"`
}
