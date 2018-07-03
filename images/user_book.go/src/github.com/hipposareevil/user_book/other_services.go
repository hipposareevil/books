package main


///////////////////
// Set of functions to make calls to other services


import (
	"encoding/json"
	"fmt"
	_ "github.com/go-sql-driver/mysql"
	"io/ioutil"
	"net/http"
	"strings"
    "net/url"
	"time"
    "strconv"
)



////////////
// Query the /tag endpoint for all tags
//
// returns Tags, which has array of Tag objects
func getAllTags(bearer string) Tags {
	start := time.Now()

	fullUrl := "http://tag:8080/tag"

	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	req, err := http.NewRequest(http.MethodGet, fullUrl, nil)
	if err != nil {
		fmt.Println("Unable to make new request to /tag")
		return Tags{}
	}

	// set headers
	req.Header.Set("User-Agent", "user-book-service-client")
	req.Header.Set("authorization", "Bearer "+bearer)

	// send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
		fmt.Println("Unable to send request to /tag")
		return Tags{}
	}

	// Check status code
	if !strings.Contains(res.Status, "200") {
		fmt.Println("Unable to connect to '" + fullUrl + "' to get names. HTTP code: " + res.Status)
		return Tags{}
	}

	// parse body
	body, readErr := ioutil.ReadAll(res.Body)
	if readErr != nil {
		fmt.Println("Unable to parse response from /tag")
		return Tags{}
	}

	// get tags
    allTags := Tags{}
	jsonErr := json.Unmarshal(body, &allTags)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /tag")
		return Tags{}
	}

	t := time.Now()
	elapsed := t.Sub(start)
	fmt.Println("get tags -> Elapsed: ", elapsed)

    return allTags
}

///////////
// Query the /book endpoint for a single book
func getBook(bearer string, bookId int, userBook *UserBook) (error) {
	start := time.Now()

    bookIdAsString := strconv.Itoa(bookId)

	fullUrl := "http://book:8080/book/" + bookIdAsString

	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	req, err := http.NewRequest(http.MethodGet, fullUrl, nil)
	if err != nil {
		fmt.Println("Unable to make new request to /book")
		return ErrServerError
	}

	// set headers
	req.Header.Set("User-Agent", "user-book-service-client")
	req.Header.Set("authorization", "Bearer "+bearer)

	// send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
		fmt.Println("Unable to send request to /book")
		return ErrServerError
	}

	// Check status code
	if !strings.Contains(res.Status, "200") {
		fmt.Println("getBook: Unable to connect to '" + fullUrl + "' to get names. HTTP code: " + res.Status)
        return nil
	}

	// parse body
	body, readErr := ioutil.ReadAll(res.Body)
	if readErr != nil {
		fmt.Println("Unable to parse response from /book")
        return nil
	}

	// get books
    book := Book{}
	jsonErr := json.Unmarshal(body, &book)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /book")
		return  nil
	}

	t := time.Now()
	elapsed := t.Sub(start)
	fmt.Println("get book from userbook -> Elapsed: ", elapsed)

    // Set values on incoming UserBook
    userBook.Title = book.Title
    userBook.AuthorName = book.AuthorName
    userBook.AuthorId = book.AuthorId
    userBook.FirstPublishedYear = book.FirstPublishedYear
    userBook.ImageSmall = book.ImageSmall
    userBook.ImageMedium = book.ImageMedium
    userBook.ImageLarge = book.ImageLarge
    
    return nil
}

///////////
// Query the /book endpoint for all books that match the incoming title
func getBooksByTitle(bearer string, title string) (Books, error) {
	start := time.Now()

    title = url.QueryEscape(title)

	fullUrl := "http://book:8080/book?title=" + title
    fmt.Println("Making title query with url '",fullUrl,"'")

	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	req, err := http.NewRequest(http.MethodGet, fullUrl, nil)
	if err != nil {
		fmt.Println("Unable to make new request to /book")
		return Books{}, ErrServerError
	}

	// set headers
	req.Header.Set("User-Agent", "user-book-service-client")
	req.Header.Set("authorization", "Bearer "+bearer)

	// send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
		fmt.Println("Unable to send request to /book")
		return Books{},ErrServerError
	}

	// Check status code
	if !strings.Contains(res.Status, "200") {
		fmt.Println("getBooksByTitle: Unable to connect to '" + fullUrl + "' to get names. HTTP code: " + res.Status)
		return Books{},ErrServerError
	}

	// parse body
	body, readErr := ioutil.ReadAll(res.Body)
	if readErr != nil {
		fmt.Println("Unable to parse response from /book")
		return Books{},ErrServerError
	}

	// get books
    books := Books{}
	jsonErr := json.Unmarshal(body, &books)
	if jsonErr != nil {
		fmt.Println("Unable to unmarshall response from /book")
		return Books{}, ErrServerError
	}

	t := time.Now()
	elapsed := t.Sub(start)
	fmt.Println("get books by title -> Elapsed: ", elapsed)
    
    return books, nil
}

