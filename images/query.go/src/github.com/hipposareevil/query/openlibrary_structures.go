package main

//////////////////////////
// Responses from from openlibrary API

// This contains the individual author structs
type OpenLibraryAuthors struct {
    Start int `json:"start"`
    NumFound int `json:"numFound"`
    Data []OpenLibraryAuthor `json:"docs"`
}

// Individual Author response
type OpenLibraryAuthor struct {
    Subjects    []string `json:"top_subjects"`
    Name string `json:"name"`
    OlKey string `json:"key"`
    BirthDate string `json:"birth_date"`
}
