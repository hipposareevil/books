package main

//////////////////////
// Structures

type queryAuthorRequest struct {
	Offset int    `json:"offset"`
	Limit  int    `json:"limit"`
	Author string `json:"author"`
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
