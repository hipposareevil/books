package main

///////////////////
// Structures

// GET request for users, contains:
// - offset
// - limit
type getAllUsersRequest struct {
	Offset int `json:"offset"`
	Limit  int `json:"limit"`
}

// GET request for single user
// - user_id
type getUserRequest struct {
	UserId int `json:"user_id"`
}

// DELETE request for single user
// - user_id
type deleteUserRequest struct {
	UserId int `json:"user_id"`
}

// POST request to create user
type createUserRequest struct {
	Name      string `json:"name"`
	UserGroup string `json:"userGroup"`
	Data      string `json:"data"`
	Password  string `json:"password"`
}

// PUT request to update user
// struct passed into service
type updateUserRequest struct {
	Name      string `json:"name"`
	UserGroup string `json:"userGroup"`
	Data      string `json:"data"`
	Password  string `json:"password"`
	Id        int    `json:"id,omitempty"`
}

//// Response structures

type User struct {
	Id        int    `json:"id"`
	Name      string `json:"name"`
	Data      string `json:"data"`
	UserGroup string `json:"userGroup"`
}

type Users struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Total  int    `json:"total"`
	Data   []User `json:"data"`
}
