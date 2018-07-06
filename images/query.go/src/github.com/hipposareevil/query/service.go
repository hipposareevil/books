package main

// Query service

import (
	"fmt"
	"net/http"
	"time"
    "strings"
    "encoding/json"
	"io/ioutil"
    "net/url"
)


// Service interface exposed to clients
type QueryService interface {
    QueryAuthor(string, int, int) (Authors, error)
}

////////////////////////
// Actual service
type queryService struct {}

//////////
// METHODS on queryService


// Constants for URLs in openlibrary
const BASE_AUTHOR_URL = "https://openlibrary.org/search/authors?q="
const ROOT_AUTHOR_IMAGE = "https://covers.openlibrary.org/a/olid/"


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
        newSubjects :=  make([]string, 0, len(current.Subjects))
        for _, subject := range current.Subjects {
            if ((! strings.Contains(subject, "Accessible book")) &&
                (! strings.Contains(subject, "Protected DAISY")) ) {
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
            BirthDate: current.BirthDate,
            Name: current.Name,
            OlKey: current.OlKey,
            Subjects: newSubjects,
            ImageSmall: smallImage,
            ImageMedium: mediumImage,
            ImageLarge: largeImage,
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
    if (limit <= 0) {
        limit = 20
    }

    // determine slice of datum to use
    whereToEnd := offset + limit
    if (whereToEnd > realNumberRows) {
        whereToEnd = realNumberRows
    }

    datum = datum[offset:whereToEnd]

    authorsToReturn := Authors {
		Offset: offset,
		Limit:  len(datum),
		Total:  realNumberRows,
		Data:   datum,
    }

	return authorsToReturn, nil
}




//////////////////////////////////////////////////


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
