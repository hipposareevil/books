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


// namespace for looking up tags.
const TAG_CACHE="tag"
// namespace for looking up books by book id.
const BOOK_CACHE="book.info"


////////////
// Query the /tag endpoint for all tags
//
// cache: Cache to utilize
// bearer: BEARER string used to connect to other web service
// 
// returns Tags, which has array of Tag objects
func getAllTags(cache CacheLayer, bearer string) Tags {
    // Check the cache
    tagsAsBytes := cache.GetBytes(TAG_CACHE, 0)
    if len(tagsAsBytes) > 0 {
        allTags := Tags{}
        err := json.Unmarshal(tagsAsBytes, &allTags)
        if err == nil {
            // Check that there are actual tags in this data
            numTags := len(allTags.Data)
            if numTags > 0 {
                fmt.Println("Got ",numTags," 'tags' from cache")
                return allTags                
            } else {
                fmt.Println("Not using 'tags' from cache as it is empty")
            }
        }
    }

    ///////////
    // Get from tag service

	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	fullUrl := "http://tag:8080/tag"

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

    return allTags
}

///////////
// Query the /book endpoint for a single book
//
// params:
// cache: Cache to utilize
// bearer: BEARER string used to connect to other web service
// bookId: ID of book to get
// userBook: UserBook to fill in with book data retrieved from /book service
// 
func getBookById(cache CacheLayer, bearer string, bookId int, userBook *UserBook) (error) {
    // Check the cache
    bookAsBytes := cache.GetBytes(BOOK_CACHE, bookId)
    if len(bookAsBytes) > 0 {
        book := Book{}

        err := json.Unmarshal(bookAsBytes, &book)
        if err == nil {
            fmt.Println("Got 'book' from cache for bookid:", bookId)

            // Fill in book info
            userBook.Title = book.Title
            userBook.AuthorName = book.AuthorName
            userBook.AuthorId = book.AuthorId
            userBook.FirstPublishedYear = book.FirstPublishedYear
            userBook.ImageSmall = book.ImageSmall
            userBook.ImageMedium = book.ImageMedium
            userBook.ImageLarge = book.ImageLarge

            return nil
        }
    }


    ///////////
    // Get from book service
    
    // book id as string
    bookIdAsString := strconv.Itoa(bookId)

	// make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

	// make request object
	fullUrl := "http://book:8080/book/" + bookIdAsString


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

    return books, nil
}

