package main


///////////////////
// Structures

// GET request for author, contains:
// - offset
// - limit
// - bearer
type getAllAuthorsRequest struct {
	Offset int `json:"offset"`
	Limit  int `json:"limit"`
}

// GET request for single author
// - bearer
// - author_id
type getAuthorRequest struct {
	AuthorId int `json:"author_id"`
}

// DELETE request for single author
// - bearer
// - author_id
type deleteAuthorRequest struct {
	AuthorId int `json:"author_id"`
}

// POST request to create author
type createAuthorRequest struct {
	Name         string   `json:"name"`
	BirthDate    string   `json:"birthDate"`
	OlKey        string   `json:"olKey"`
	GoodReadsUrl string   `json:"goodreadsUrl"`
	ImageSmall   string   `json:"imageSmall"`
	ImageMedium  string   `json:"imageMedium"`
	ImageLarge   string   `json:"imageLarge"`
	Subjects     []string `json:"subjects"`
}

// PUT request to update author
// struct passed into service
type updateAuthorRequest struct {
	Name         string   `json:"name"`
	Id           int      `json:"id,omitempty"`
	BirthDate    string   `json:"birthDate"`
	OlKey        string   `json:"olKey"`
	GoodReadsUrl string   `json:"goodreadsUrl"`
	ImageSmall   string   `json:"imageSmall"`
	ImageMedium  string   `json:"imageMedium"`
	ImageLarge   string   `json:"imageLarge"`
	Subjects     []string `json:"subjects"`
}

//// Response structures

type Author struct {
	Id           int      `json:"id"`
	Name         string   `json:"name"`
	BirthDate    string   `json:"birthDate"`
	OlKey        string   `json:"olKey"`
	GoodReadsUrl string   `json:"goodreadsUrl"`
	ImageSmall   string   `json:"imageSmall"`
	ImageMedium  string   `json:"imageMedium"`
	ImageLarge   string   `json:"imageLarge"`
	Subjects     []string `json:"subjects"`
}

type Authors struct {
	Offset int      `json:"offset"`
	Limit  int      `json:"limit"`
	Total  int      `json:"total"`
	Data   []Author `json:"data"`
}
