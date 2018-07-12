package main

///////////////////
// Request Structures

// GET request for all user book, contains:
type getReviewsRequest struct {
	Bearer string `json:"bearer"`
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	BookId int    `json:"book_id"`
}

//////////////////////////////////
// Response structures
//////////////////////////////////

type Review struct {
	BookId   int      `json:"bookId"`
	Rating   bool     `json:"rating"`
	Tags     []string `json:"tags"`
	Review   string   `json:"review"`
	UserName string   `json:"userName"`
	UserId   int      `json:"UserId"`
}

type Reviews struct {
	Offset int      `json:"offset"`
	Limit  int      `json:"limit"`
	Total  int      `json:"total"`
	Data   []Review `json:"data"`
}

///////////////////////
// Structures from other services
//
// These will be truncated versions from the other services
// as we don't want every field
///////////////////////

type User struct {
	Id   int    `json:"id"`
	Name string `json:"name"`
}

type Users struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Total  int    `json:"total"`
	Data   []User `json:"data"`
}

type UserBook struct {
	BookId int      `json:"bookId"`
	Rating bool     `json:"rating"`
	Tags   []string `json:"tags"`
	Review string   `json:"review"`
}

type UserBooks struct {
	Offset int        `json:"offset"`
	Limit  int        `json:"limit"`
	Total  int        `json:"total"`
	Data   []UserBook `json:"data"`
}
