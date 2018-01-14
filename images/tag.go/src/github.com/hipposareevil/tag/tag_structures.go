package main

////////////////////////////
// Structures

// Single tag
// JSON Response sent to client
type Tag struct {
	ID   int    `json:"id"`
	Name string `json:"name"`
}

// List of Tags
// JSON Response sent to client
type Tags struct {
	Offset int   `json:"offset"`
	Limit  int   `json:"limit"`
	Total  int   `json:"total"`
	Data   []Tag `json:"data"`
}

// Tag to be created or updated
// Used in POST or PUT
type NewTag struct {
	Name string `json:"name"`
}
