package main

// Query service

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"sort"
	"strconv"
	"strings"
	"time"
)

// Service interface exposed to clients
type QueryService interface {
	QueryAuthor(string, int, int) (Authors, error)
	QueryTitle(string, string, string, int, int) (Titles, error)
}

////////////////////////
// Actual service
type queryService struct{}

//////////
// METHODS on queryService

// Constants for URLs in openlibrary
const BASE_AUTHOR_URL = "https://openlibrary.org/search/authors?q="
const BASE_TITLE_URL = "https://openlibrary.org/search?"

const ROOT_URL = "https://openlibrary.org"
const WORKS_BASE_URL = "https://openlibrary.org"

// for images
const ROOT_AUTHOR_IMAGE = "https://covers.openlibrary.org/a/olid/"
const ROOT_COVER_IMAGE = "https://covers.openlibrary.org/b/"

////////////////
// Query Authors
//
// returns:
// Authors
// error
func (theService queryService) QueryAuthor(authorName string, offset int, limit int) (Authors, error) {
	fmt.Println(" ")
	fmt.Println("-- QueryAuthor --")

	authorName = url.QueryEscape(authorName)
	queryUrl := BASE_AUTHOR_URL + authorName
	fmt.Println("looking for:", queryUrl)

	// Make request to openlibrary
	body, err := makeRequest(queryUrl)
	if err != nil {
		fmt.Println("Unable to make request to openlibrary for authors: ", err)
		return Authors{}, err
	}

	// parse Authors response
	openlibraryAuthors := OpenLibraryAuthors{}
	jsonErr := json.Unmarshal(body, &openlibraryAuthors)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from openlibrary for author query:", jsonErr)
		return Authors{}, err
	}

	// data to return
	datum := make([]Author, 0, 0)

	// Convert openlibrary response into our structure
	for _, current := range openlibraryAuthors.Data {
		// Parse this author

		// Update subjects to not have a few entries we don't want
		newSubjects := make([]string, 0, len(current.Subjects))
		for _, subject := range current.Subjects {
			if (!strings.Contains(subject, "Accessible book")) &&
				(!strings.Contains(subject, "Protected DAISY")) {
				// add to subjects
				newSubjects = append(newSubjects, subject)
			}
		}

		// images
		smallImage := ROOT_AUTHOR_IMAGE + current.OlKey + "-S.jpg"
		mediumImage := ROOT_AUTHOR_IMAGE + current.OlKey + "-M.jpg"
		largeImage := ROOT_AUTHOR_IMAGE + current.OlKey + "-L.jpg"

		// Create author to return
		newAuthor := Author{
			BirthDate:   current.BirthDate,
			Name:        current.Name,
			OlKey:       current.OlKey,
			Subjects:    newSubjects,
			ImageSmall:  smallImage,
			ImageMedium: mediumImage,
			ImageLarge:  largeImage,
		}
		// Add to data
		datum = append(datum, newAuthor)
	}

	////////////////////
	// Update the offset/limit

	// Get the total number of rows
	realNumberRows := len(openlibraryAuthors.Data)

	// fix offset
	if (offset > realNumberRows) || (offset < 0) {
		offset = 0
	}

	// fix limit
	if limit <= 0 {
		limit = 20
	}

	// determine slice of datum to use
	whereToEnd := offset + limit
	if whereToEnd > realNumberRows {
		whereToEnd = realNumberRows
	}

	datum = datum[offset:whereToEnd]

	authorsToReturn := Authors{
		Offset: offset,
		Limit:  len(datum),
		Total:  realNumberRows,
		Data:   datum,
	}

	return authorsToReturn, nil
}

////////////////
// Query Titles
//
// returns:
// Titles
// error
func (theService queryService) QueryTitle(authorName string, title string, isbn string, offset int, limit int) (Titles, error) {
	fmt.Println(" ")
	fmt.Println("-- QueryTitle --")

	// base url for titles
	queryUrl := BASE_TITLE_URL

	// string used in concating
	and := ""

	if len(title) > 0 {
		title = url.QueryEscape(title)
		queryUrl += and + "title=" + title
		and = "&"
	}
	if len(isbn) > 0 {
		isbn = url.QueryEscape(isbn)
		queryUrl += and + "isbn=" + isbn
		and = "&"
	}
	if len(authorName) > 0 {
		authorName = url.QueryEscape(authorName)
		queryUrl += and + "author=" + authorName
		and = "&"
	}
	fmt.Println("looking for:", queryUrl)

	// Make request to openlibrary
	body, err := makeRequest(queryUrl)
	if err != nil {
		fmt.Println("Unable to make request to openlibrary for titles: ", err)
		return Titles{}, err
	}

	// parse Titles response
	openlibraryTitles := OpenLibraryTitles{}
	jsonErr := json.Unmarshal(body, &openlibraryTitles)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from openlibrary for title query:", jsonErr)
		return Titles{}, err
	}

	// data to return
	datum := make([]Title, 0, 0)

	// Convert openlibrary response into our structure
	for _, current := range openlibraryTitles.Data {
		// Parse this title

		fmt.Println("")
		fmt.Println("Current: ", current)

		// Update subjects to not have a few entries we don't want
		newSubjects := make([]string, 0, len(current.Subjects))
		for _, subject := range current.Subjects {
			if (!strings.Contains(subject, "Accessible book")) &&
				(!strings.Contains(subject, "In library")) &&
				(!strings.Contains(subject, "Protected DAISY")) {
				// add to subjects
				newSubjects = append(newSubjects, subject)
			}
		}

		// description
		description := getDescription(current.WorksKey)

		// work url
		workUrl := ROOT_URL + current.WorksKey

		// author name
		var authorName string
		if len(current.AuthorNames) > 0 {
			authorName = current.AuthorNames[0]
		}

		// author key
		var authorKey string
		if len(current.AuthorKeys) > 0 {
			authorKey = current.AuthorKeys[0]
		}

		// images
		smallImage := createCoverImage(current, "-S.jpg")
		mediumImage := createCoverImage(current, "-M.jpg")
		largeImage := createCoverImage(current, "-L.jpg")

		// Create title to return
		newTitle := Title{
			// These are derived
			AuthorKey:          authorKey,
			AuthorName:         authorName,
			Description:        description,
			OpenLibraryWorkUrl: workUrl,
			ImageSmall:         smallImage,
			ImageMedium:        mediumImage,
			ImageLarge:         largeImage,

			// These are passed through
			Title:              current.Title,
			FirstPublishedYear: current.FirstPublishYear,
			OpenLibraryKeys:    current.EditionKeys,
			Isbns:              current.Isbns,
			Subjects:           current.Subjects,
		}

		// Add to data
		datum = append(datum, newTitle)
	}

	// Sort datum by # of isbns
	sort.Slice(datum, func(i, j int) bool {
		return len(datum[i].Isbns) > len(datum[j].Isbns)
	})

	////////////////////
	// Update the offset/limit

	// Get the total number of rows
	realNumberRows := len(openlibraryTitles.Data)

	// fix offset
	if (offset > realNumberRows) || (offset < 0) {
		offset = 0
	}

	// fix limit
	if limit <= 0 {
		limit = 20
	}

	// determine slice of datum to use
	whereToEnd := offset + limit
	if whereToEnd > realNumberRows {
		whereToEnd = realNumberRows
	}

	datum = datum[offset:whereToEnd]

	titlesToReturn := Titles{
		Offset: offset,
		Limit:  len(datum),
		Total:  realNumberRows,
		Data:   datum,
	}

	return titlesToReturn, nil
}

//////////////////////////////////////////////////

// Create image for a title/book
//
// params:
// book: book to get cover for
// size: size string
func createCoverImage(book OpenLibraryTitle, size string) string {
	fmt.Println("")
	fmt.Println("Create image for book: ", book.CoverImage)

	// Check the cover image value itself
	if book.CoverImage > 0 {
		imageUrl := ROOT_COVER_IMAGE + "id/" + strconv.Itoa(book.CoverImage) + size
		return imageUrl
	}

	// If that fails, try the first ISBN
	if len(book.Isbns) > 0 {
		firstIsbn := book.Isbns[0]
		imageUrl := ROOT_COVER_IMAGE + "isbn/" + firstIsbn + size
		return imageUrl
	}

	return ""
}

// Get description by querying openlibrary again
//
// params:
// worksKey: openlibrary works key
func getDescription(worksKey string) string {
	fmt.Println("Get description for works key: ", worksKey)

	queryUrl := WORKS_BASE_URL + worksKey

	fmt.Println("get description via url: ", queryUrl)

	// Make request to openlibrary
	body, err := makeRequest(queryUrl)
	if err != nil {
		fmt.Println("Unable to make request to openlibrary for getting description: ", err)
		return ""
	}

	// parse response
	description := OpenLibraryDescription{}
	jsonErr := json.Unmarshal(body, &description)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from openlibrary for description query:", jsonErr)
		return ""
	}

	return description.Description.Value
}

// Perform the boilerplate portion of making an http request
//
// param:
// URL to query
//
func makeRequest(queryUrl string) ([]byte, error) {
	///////////////
	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	req, err := http.NewRequest(http.MethodGet, queryUrl, nil)
	if err != nil {
		fmt.Println("Unable to make new request to openlibrary for url: ", queryUrl, " error: ", err)
		return nil, err
	}

	// set headers
	req.Header.Set("User-Agent", "query-service-client")
	req.Header.Set("accept", "application/json")
	req.Header.Set("content-type", "application/json")

	// send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
		fmt.Println("Unable to make send request to openlibrary for url: ", queryUrl, " error: ", getErr)
		return nil, err
	}

	// Check status code
	if !strings.Contains(res.Status, "200") {
		fmt.Println("Unable to connect to  openlibrary for url: ", queryUrl, " HTTP status: ", res.Status)
		return nil, err
	}

	// parse body
	body, readErr := ioutil.ReadAll(res.Body)
	if readErr != nil {
		fmt.Println("Unable to parse response from openlibrary: ", readErr)
		return nil, err
	}

	return body, nil

}
