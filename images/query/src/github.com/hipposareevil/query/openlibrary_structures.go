package main

//////////////////////////
// Responses from from openlibrary API

// This contains the individual author structs
type OpenLibraryAuthors struct {
	Start    int                 `json:"start"`
	NumFound int                 `json:"numFound"`
	Data     []OpenLibraryAuthor `json:"docs"`
}

// Individual Author response
type OpenLibraryAuthor struct {
	Subjects  []string `json:"top_subjects"`
	Name      string   `json:"name"`
	OlKey     string   `json:"key"`
	BirthDate string   `json:"birth_date"`
}

// This contains the individual title structs
type OpenLibraryTitles struct {
	Start    int                `json:"start"`
	NumFound int                `json:"numFound"`
	Data     []OpenLibraryTitle `json:"docs"`
}

// Individual Title response
type OpenLibraryTitle struct {
	Title            string   `json:"title_suggest"`
	CoverImage       int      `json:"cover_i"`
	FirstPublishYear int      `json:"first_publish_year"`
	WorksKey         string   `json:"key"`
	AuthorKeys       []string `json:"author_key"`
	AuthorNames      []string `json:"author_name"`
	Subjects         []string `json:"subject"`
	Isbns            []string `json:"isbn"`
	PublishYears     []int    `json:"publish_year"`
	EditionKeys      []string `json:"edition_key"`
}

// Description JSON returned for a given works url
type MetaDescription struct {
	Type  string `json:"Type"`
	Value string `json:"value"`
}

type OpenLibraryDescription struct {
	Description MetaDescription `json:"description"`
	Subject     []string        `json:"subjects"`
}
