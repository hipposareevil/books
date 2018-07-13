package main

///////////////////
// Set of functions to make calls to other services

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"time"
    "net/url"
)

// Struct returned by /author service
type Author struct {
	Name     string `json:"name"`
	AuthorId int    `json:"id"`
}


type Authors struct {
	Offset int      `json:"offset"`
	Limit  int      `json:"limit"`
	Total  int      `json:"total"`
	Data   []Author `json:"data"`
}



////////////
// Query the /author endpoint for author information by name
// will return array of IDs
//  
func getAuthorIdsByName(bearer string, authorName string) []int {
    var ids []int

    // Make request to other service
    authorName = url.QueryEscape(authorName)

	fullUrl := "http://author:8080/author?name=" + authorName
    body, err := makeRequest(bearer, fullUrl)
    if err != nil {
        fmt.Println("Unable to make request to /author: ", err)
        return ids
    }

	// get author info
    authors := Authors{}
	jsonErr := json.Unmarshal(body, &authors)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /author")
		return ids
	}

    // get each author
    for _, current := range authors.Data {
        ids = append(ids, current.AuthorId)
    }

    return ids
}




////////////
// Query the /author endpoint for author name via ID
// 
func getAuthorNameById(cache CacheLayer, bearer string, authorId int) string {
	// Check cache
	authorName := cache.Get(AUTHOR_CACHE, authorId)
	if len(authorName) > 0 {
		fmt.Println("Got Author name from cache.")
		return authorName
	}

    // no cache

    // Make request to other service
	fullUrl := "http://author:8080/author/" + strconv.Itoa(authorId)
    body, err := makeRequest(bearer, fullUrl)
    if err != nil {
        fmt.Println("Unable to make request to /author: ", err)
        return ""
    }

	// get author info
	authorBean := Author{}
	jsonErr := json.Unmarshal(body, &authorBean)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /author")
		return ""
	}

	return authorBean.Name
}




// Perform the boilerplate portion of making an http request
//
// param:
// bearer
// URL to query
// 
func makeRequest(bearer string, queryUrl string) ([]byte, error) {
    ///////////////
	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	req, err := http.NewRequest(http.MethodGet, queryUrl, nil)
	if err != nil {
		fmt.Println("Unable to make new request to url: ", queryUrl, " error: ", err)
		return nil, err
	}

	// set headers
	req.Header.Set("User-Agent", "review-service-client")
	req.Header.Set("accept", "application/json")
	req.Header.Set("content-type", "application/json")
	req.Header.Set("authorization", "Bearer "+bearer)


	// send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
		fmt.Println("Unable to make send request to url: ", queryUrl, " error: ", getErr)
		return nil, err
	}

	// Check status code
	if !strings.Contains(res.Status, "200") {
		fmt.Println("Unable to connect to  url: ", queryUrl, " HTTP status: ", res.Status)
		return nil, err
	}

	// parse body
	body, readErr := ioutil.ReadAll(res.Body)
	if readErr != nil {
		fmt.Println("Unable to parse response from url: ", queryUrl, " :", readErr)
		return nil, err
	}

    return body, nil

}



