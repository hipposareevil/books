package main

//////////////////////
// Structures

type queryAuthorRequest struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Author string `json:"author"`
}

type queryTitleRequest struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Author string `json:"author"`
	Title  string `json:"title"`
	Isbn   string `json:"isbn"`
}

//////////////////////////////////
// Response structures
//////////////////////////////////

type Author struct {
	BirthDate   string   `json:"birthDate"`
	ImageLarge  string   `json:"imageLarge"`
	ImageMedium string   `json:"imageMedium"`
	ImageSmall  string   `json:"imageSmall"`
	Name        string   `json:"name"`
	OlKey       string   `json:"olKey"`
	Subjects    []string `json:"subjects"`
}

type Authors struct {
	Offset int      `json:"offset"`
	Limit  int      `json:"limit"`
	Total  int      `json:"total"`
	Data   []Author `json:"data"`
}

type Title struct {
	Title              string   `json:"title"`
	AuthorKey          string   `json:"authorKey"`
	AuthorName         string   `json:"authorName"`
	Description        string   `json:"description"`
	FirstPublishedYear int      `json:"firstPublishedYear"`
	ImageLarge         string   `json:"imageLarge"`
	ImageMedium        string   `json:"imageMedium"`
	ImageSmall         string   `json:"imageSmall"`
	OpenLibraryWorkUrl string   `json:"openLibraryWorkUrl"`
	OpenLibraryKeys    []string `json:"openLibraryKeys"`
	Isbns              []string `json:"isbns"`
	Subjects           []string `json:"subjects"`
}

type Titles struct {
	Offset int     `json:"offset"`
	Limit  int     `json:"limit"`
	Total  int     `json:"total"`
	Data   []Title `json:"data"`
}
