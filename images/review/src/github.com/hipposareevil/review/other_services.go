package main

///////////////////
// Set of functions to make calls to other services

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"time"
)

///////////
// Query the /user endpoint for all user names and IDs
//
func getUsers(bearer string) (Users, error) {
	fullUrl := "http://books.user:8080/user"
	fmt.Println("Making user query with url '", fullUrl, "'")

	// Make request to other service
	body, err := makeRequest(bearer, fullUrl)
	if err != nil {
		fmt.Println("Unable to make request to /users: ", err)
		return Users{}, err
	}

	// parse Users response
	users := Users{}
	jsonErr := json.Unmarshal(body, &users)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /users:", jsonErr)
		return Users{}, err
	}

	return users, nil
}

/////////
// Query the /user_book endpoint to get a userbook for a
// specified user and book
//
func getUserBook(bearer string, userId int, bookId int) (UserBook, error) {
	userIdAsString := strconv.Itoa(userId)
	bookIdAsString := strconv.Itoa(bookId)

	fullUrl := "http://books.user_book:8080/user_book/" + userIdAsString + "?book_id=" + bookIdAsString
	fmt.Println("Making user query with url '", fullUrl, "'")

	// Make request to other service
	body, err := makeRequest(bearer, fullUrl)
	if err != nil {
		fmt.Println("Unable to make request to /user_book: ", err)
		return UserBook{}, err
	}

	// parse UserBooks response
	userBooks := UserBooks{}
	jsonErr := json.Unmarshal(body, &userBooks)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /user_books:", jsonErr)
		return UserBook{}, err
	}

	numData := len(userBooks.Data)
	userBook := UserBook{}

	if numData > 0 {
		userBook = userBooks.Data[0]
		return userBook, nil
	} else {
		fmt.Println("No userbooks for user ", userId, " for book ", bookId)
		return userBook, errors.New("No userbook for user ")
	}
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
