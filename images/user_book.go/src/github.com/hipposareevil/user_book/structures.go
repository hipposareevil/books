package main

import (
	"time"
)

///////////////////
// Structures

// GET request for all user book, contains:
type getAllUserBooksRequest struct {
	Bearer string   `json:"bearer"`
	Offset int      `json:"offset"`
	Limit  int      `json:"limit"`
	UserId int      `json:"user_id"`
	Title  string   `json:"title"`
	BookId int      `json:"book_id"`
	Tag    []string `json:"tag"`
}

// GET request for single user book
type getUserBookRequest struct {
	Bearer     string `json:"bearer"`
	UserId     int    `json:"user_id"`
	UserBookId int    `json:"user_book_id"`
}

// DELETE request for single user book
type deleteUserBookRequest struct {
	UserId     int `json:"user_id"`
	UserBookId int `json:"user_book_id"`
}

// POST request to create user book
type createUserBookRequest struct {
	Bearer string   `json:"bearer"`
	UserId int      `json:"user_id"`
	BookId int      `json:"bookId"`
	Rating bool     `json:"rating"`
	Tags   []string `json:"tags"`
}

// PUT request to update user book
type updateUserBookRequest struct {
	Bearer     string   `json:"bearer"`
	UserId     int      `json:"user_id"`
	UserBookId int      `json:"user_book_id"`
	BookId     int      `json:"bookId"`
	Rating     bool     `json:"rating"`
	Tags       []string `json:"tags"`
}

//////////////////////////////////
// Response structures
//////////////////////////////////

type UserBook struct {
	BookId             int       `json:"bookId"`
	Rating             bool      `json:"rating"`
	Tags               []string  `json:"tags"`
	UserId             int       `json:"userId"`
	UserBookId         int       `json:"userBookId"`
	DateAdded          time.Time `json:"dateAdded"`
	Title              string    `json:"title"`
	AuthorName         string    `json:"authorName"`
	AuthorId           int       `json:"authorId"`
	FirstPublishedYear int       `json:"firstPublishedYear"`
	ImageLarge         string    `json:"imageLarge"`
	ImageMedium        string    `json:"imageMedium"`
	ImageSmall         string    `json:"imageSmall"`
}

type UserBooks struct {
	Offset int        `json:"offset"`
	Limit  int        `json:"limit"`
	Total  int        `json:"total"`
	Data   []UserBook `json:"data"`
}


//////////////////////////////////////
// json structures from calling other services
//


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


// Single book
type Book struct {
	AuthorId           int      `json:"authorId"`
	AuthorName         string   `json:"authorName"`
	Description        string   `json:"description"`
	FirstPublishedYear int      `json:"firstPublishedYear"`
	GoodReadsUrl       string   `json:"goodreadsUrl"`
	Id                 int      `json:"id"`
	ImageLarge         string   `json:"imageLarge"`
	ImageMedium        string   `json:"imageMedium"`
	ImageSmall         string   `json:"imageSmall"`
	Isbns              []string `json:"isbns"`
	OpenlibraryWorkUrl string   `json:"openlibraryWorkUrl"`
	Subjects           []string `json:"subjects"`
	Title              string   `json:"title"`
}


type Books struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Total  int    `json:"total"`
	Data   []Book `json:"data"`
}
