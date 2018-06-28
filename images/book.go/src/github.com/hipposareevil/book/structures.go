package main

///////////////////
// Structures

// GET request for book, contains:
// - offset
// - limit
type getAllBooksRequest struct {
	Bearer string `json:"bearer"`
	Offset   int    `json:"offset"`
	Limit    int    `json:"limit"`
	Title    string `json:"title"`
	AuthorId []int  `json:"author_id"`
	BookId   []int  `json:"book_id"`
}

// GET request for single book
// - book_id
type getBookRequest struct {
	Bearer string `json:"bearer"`
	BookId int `json:"book_id"`
}

// DELETE request for single book
// - book_id
type deleteBookRequest struct {
	BookId int `json:"book_id"`
}

// POST request to create book
type createBookRequest struct {
	Bearer string `json:"bearer"`
	AuthorId           int      `json:"authorId"`
	Description        string   `json:"description"`
	FirstPublishedYear int      `json:"firstPublishedYear"`
	GoodReadsUrl       string   `json:"goodreadsUrl"`
	ImageLarge         string   `json:"imageLarge"`
	ImageMedium        string   `json:"imageMedium"`
	ImageSmall         string   `json:"imageSmall"`
	Isbns              []string `json:"isbns"`
	OpenlibraryWorkUrl string   `json:"openlibraryWorkUrl"`
	Subjects           []string `json:"subjects"`
	Title              string   `json:"title"`
}

// PUT request to update book
// struct passed into service
type updateBookRequest struct {
	Bearer string `json:"bearer"`
    BookId             int      `json:"id"`
	AuthorId           int      `json:"authorId"`
	Description        string   `json:"description"`
	FirstPublishedYear int      `json:"firstPublishedYear"`
	GoodReadsUrl       string   `json:"goodreadsUrl"`
	ImageLarge         string   `json:"imageLarge"`
	ImageMedium        string   `json:"imageMedium"`
	ImageSmall         string   `json:"imageSmall"`
	Isbns              []string `json:"isbns"`
	OpenlibraryWorkUrl string   `json:"openlibraryWorkUrl"`
	Subjects           []string `json:"subjects"`
	Title              string   `json:"title"`
}

//// Response structures

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
