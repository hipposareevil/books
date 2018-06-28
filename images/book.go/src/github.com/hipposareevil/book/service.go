package main

// Book service

import (
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"time"
	_ "github.com/go-sql-driver/mysql"
)

const AUTHOR_CACHE="author.name"


// Service interface exposed to clients
type BookService interface {
	// GetBooks: offset, limit, title, authorIds, titleIds
    // first param: bearer
	GetBooks(string, int, int, string, []int, []int) (Books, error)

	// GetBook: bearer, id
	GetBook(string, int) (Book, error)

	// DeleteBook: id
	DeleteBook(int) error

	// CreateBook (see createBookRequest for params)
    // first param: bearer
	CreateBook(string, int, string, int, string, string, string, string, []string, string, []string, string) (Book, error)

	// UpdateBook
    // Same as CreateBook but the first param is the ID of book to update
    // first param: bearer
	UpdateBook(string, int, int, string, int, string, string, string, string, []string, string, []string, string) (Book, error)


    // Get author name by id
    getAuthorNameById(string, int) string 
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
type bookService struct {
	mysqlDb *sql.DB
    cache CacheLayer
}

//////////
// METHODS on bookService

////////////////
// Get Book
//
// params:
// bearer: Bookization bearer
// bookId : ID of book to get
//
// returns:
// book
// error
func (theService bookService) GetBook(bearer string, bookId int) (Book, error) {
    fmt.Println("")
    fmt.Println("-- GetBook --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Book{}, errors.New("unable to ping mysql")
	}

	// Make query
	var book Book
	var subjectAsCsv string
    var isbnsAsCsv string

	// Scan the DB info into 'book' composite variable
	err := theService.mysqlDb.
		QueryRow("SELECT " +
        "book_id, author_id, year, title, " +
        "isbn, subjects, ol_works, description, " +
        "image_small, image_medium, image_large, goodreads_url " +
        "FROM book WHERE book_id = ?", bookId).
		Scan(&book.Id, &book.AuthorId, &book.FirstPublishedYear, &book.Title,
        &isbnsAsCsv, &subjectAsCsv, &book.OpenlibraryWorkUrl, &book.Description,
        &book.ImageSmall, &book.ImageMedium, &book.ImageLarge, &book.GoodReadsUrl)

	switch {
	case err == sql.ErrNoRows:
		return Book{}, ErrNotFound
	case err != nil:
		fmt.Println("Got error from select: ", err)
		return Book{}, ErrServerError
	default:
		fmt.Println("got book!", book.Title)
	}

    // Get the author name
    if len(bearer) > 0 {
        book.AuthorName = theService.getAuthorNameById(bearer, book.AuthorId)
    }

	// Convert subjects from CSV to string array
	book.Subjects = splitCsvStringToArray(subjectAsCsv)

	// Convert isbns from CSV to string array
	book.Isbns = splitCsvStringToArray(isbnsAsCsv)

	return book, nil
}

////////////////
// Get books
//
// returns:
// books
// error
func (theService bookService) GetBooks(bearer string, offset int, limit int, title string, authorIds []int, bookIds []int) (Books, error) {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Books{}, errors.New("unable to ping mysql")
	}

	// Get total number of rows
	var totalNumberOfRows int
	_ = theService.mysqlDb.QueryRow("SELECT COUNT(*) from book").Scan(&totalNumberOfRows)

	if limit > totalNumberOfRows {
		limit = totalNumberOfRows
	}

	// Make select string
    selectString := "SELECT " +
        "book_id, author_id, year, title, " +
        "isbn, subjects, ol_works, description, " +
        "image_small, image_medium, image_large, goodreads_url " +
        "FROM book "

    fmt.Println("")
    fmt.Println("-- GetBooks --")
    fmt.Println("author: ", authorIds)
    fmt.Println("books: ", bookIds)


    // Update query according to which other queryParams come in (title, authorIds, bookIds)
    updated := false
    // Title
    if len(title) > 0 {
        updated = true
        selectString += "WHERE title LIKE '%" + title + "%' "
    }
    // Author IDs
    if len(authorIds) > 0 {
        var prependValue string
        
        if updated {
            prependValue = " AND"
        } else {
            prependValue = " WHERE"
        }
        authorIdsAsCsv := convertIntArrayToCsv(authorIds)
        selectString += prependValue + " author_id in (" + authorIdsAsCsv + ")"

        updated = true
    }
    // Book IDs
    if len(bookIds) > 0 {
        var prependValue string
        
        if updated {
            prependValue = " AND"
        } else {
            prependValue = " WHERE"
        }
        bookIdsAsCsv := convertIntArrayToCsv(bookIds)
        selectString += prependValue + " book_id in (" + bookIdsAsCsv + ")"

        updated = true
    }

    fmt.Println("Making query with: " + selectString)

    // Make query
	results, err := theService.mysqlDb.
		Query(selectString +
			"LIMIT ?,?", offset, limit)

	if err != nil {
		fmt.Println("Got error from mysql: " + err.Error())
		return Books{}, errors.New("unable to create query in mysql")
	}

	// slice of Book entities
	datum := make([]Book, 0, 0)

	// Parse results
	for results.Next() {
		var book Book

        var subjectAsCsv string
        var isbnsAsCsv string

		// For each row, scan the result into our book composite object:
		err = results.
            Scan(&book.Id, &book.AuthorId, &book.FirstPublishedYear, &book.Title,
            &isbnsAsCsv, &subjectAsCsv, &book.OpenlibraryWorkUrl, &book.Description,
            &book.ImageSmall, &book.ImageMedium, &book.ImageLarge, &book.GoodReadsUrl)

		if err != nil {
			fmt.Println("Got error from mysql when getting all books: " + err.Error())
			return Books{}, errors.New("Unable to scan mysql for all books.")
		}

        fmt.Println("Get author name for book with id: ",  book.Id)
        fmt.Println("Get author name for book with author id: ",  book.AuthorId)

        // Get the author name
        book.AuthorName = theService.getAuthorNameById(bearer, book.AuthorId)

        // Convert subjects from CSV to string array
        book.Subjects = splitCsvStringToArray(subjectAsCsv)

        // Convert isbns from CSV to string array
        book.Isbns = splitCsvStringToArray(isbnsAsCsv)


		datum = append(datum, book)
	}

	// Create Books to return
	returnValue := Books{
		Offset: offset,
		Limit:  limit,
		Total:  totalNumberOfRows,
		Data:   datum,
	}

	return returnValue, nil
}

////////////////
// Delete book
//
// params:
// bookId : ID of book to delete
//
// returns:
// error
func (theService bookService) DeleteBook(bookId int) error {
    fmt.Println("")
    fmt.Println("-- DeleteBook --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql")
	}

	// Verify the book exists, if not, throw ErrNotFound
	_, getErr := theService.GetBook("", bookId)
	if getErr != nil {
		return getErr
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM book WHERE book_id = ?", bookId)

	return err
}

////////////////
// CreateBook
//
// returns:
// book
// error
func (theService bookService) CreateBook(
    bearer string,
	authorId     int,
	description  string,
	firstPublishedYear    int,
	goodReadsUrl string,
	imageLarge   string,
	imageMedium  string,
	imageSmall   string,
	isbns        []string,
	openlibraryWorkUrl    string,
	subjects     []string,
	title        string) (Book, error) {

    fmt.Println("")
    fmt.Println("-- CreateBook --")

	////////////////////
	// verify mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Book{}, errors.New("unable to ping mysql")
	}

	// Convert []string to string as csv for database
	subjectsAsCsv := strings.Join(subjects[:], ",")
	isbnsAsCsv := strings.Join(isbns[:], ",")

	// Make insert
	stmt, err := theService.mysqlDb.
		Prepare("INSERT INTO book SET " +
        "author_id=?, year=?, title=?, isbn=?, subjects=?, " +
        "ol_works=?, goodreads_url=?, description=?, " +
        "image_small=?, image_medium=?, image_large=?")

	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return Book{}, errors.New("Unable to prepare a DB statement: ")
	}

	res, err := stmt.Exec(authorId, firstPublishedYear, title, isbnsAsCsv, subjectsAsCsv,
        openlibraryWorkUrl, goodReadsUrl, description,
        imageSmall, imageMedium, imageLarge)

	if err != nil {
		fmt.Println("Error inserting into DB: ", err)
		if strings.Contains(err.Error(), "Duplicate entry ") {
			return Book{}, ErrAlreadyExists
		} else {
			return Book{}, errors.New("Unable to run INSERT against DB: ")
		}
	}

	// get the id
	id, _ := res.LastInsertId()

    authorName := theService.getAuthorNameById(bearer, authorId)

	// Create book
	var bookToReturn Book
	bookToReturn = Book{
        AuthorId: authorId,
        AuthorName: authorName,
        Description: description,
        FirstPublishedYear:    firstPublishedYear,
        GoodReadsUrl: goodReadsUrl,
        Id: int(id),
        ImageLarge:   imageLarge,
        ImageMedium:  imageMedium,
        ImageSmall:   imageSmall,
        Isbns:        isbns,
        OpenlibraryWorkUrl:    openlibraryWorkUrl,
        Subjects:      subjects,
        Title:        title,
	}

	return bookToReturn, nil
}

////////////////
// UpdateBook
//
// returns:
// error
func (theService bookService) UpdateBook(
    bearer string,
    bookId int,
	authorId     int,
	description  string,
	firstPublishedYear    int,
	goodReadsUrl string,
	imageLarge   string,
	imageMedium  string,
	imageSmall   string,
	isbns        []string,
	openlibraryWorkUrl    string,
	subjects     []string,
	title        string) (Book, error) {

    fmt.Println("")
    fmt.Println("-- UpdateBook --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Book{}, errors.New("unable to ping mysql")
	}

    fmt.Println("Updating book by id: " , bookId)

	// Make query
	stmt, err := theService.mysqlDb.
		Prepare("UPDATE book SET " +
        "book_id=COALESCE(NULLIF(?,''),book_id), " +
        "author_id=COALESCE(NULLIF(?,''),author_id), " +
        "year=COALESCE(NULLIF(?,''),year), " +
        "title=COALESCE(NULLIF(?,''),title), " +
        "isbn=COALESCE(NULLIF(?,''),isbn), " +
        "subjects=COALESCE(NULLIF(?,''),subjects), " +
        "ol_works=COALESCE(NULLIF(?,''),ol_works), " +
        "goodreads_url=COALESCE(NULLIF(?,''),goodreads_url), " +
        "description=COALESCE(NULLIF(?,''),description), " +
        "image_small=COALESCE(NULLIF(?,''),image_small), " +
        "image_medium=COALESCE(NULLIF(?,''),image_medium), " +
        "image_large=COALESCE(NULLIF(?,''),image_large) " +
        "WHERE book_id = ?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return Book{}, errors.New("Unable to prepare a DB statement: ")
	}

	// Convert []string to string as csv for database
	subjectsAsCsv := strings.Join(subjects[:], ",")
	isbnsAsCsv := strings.Join(isbns[:], ",")

	_, err = stmt.Exec(bookId, authorId, firstPublishedYear, title,
        isbnsAsCsv, subjectsAsCsv, openlibraryWorkUrl, goodReadsUrl, description,
        imageSmall, imageMedium, imageLarge, bookId)

	if err != nil {
		fmt.Println("Error updatingDB for book: ", err)
		return Book{}, errors.New("Unable to run update against DB for book: ")
	}

    // get the book back
    bookToReturn, err := theService.GetBook(bearer, bookId)

	return bookToReturn, err
}

////////////////////////////////////////////////////
//
// Helper methods


// Just need the author name
type author struct {
	Name         string   `json:"name"`
}


////////////
// Query the /author endpoint for author information
func (theService bookService) getAuthorNameById(bearer string, authorId int) string {
    start := time.Now()

    // Check cache
    authorName := theService.cache.Get(AUTHOR_CACHE, authorId)
    if len(authorName) > 0 {
        return authorName
    }

    url := "http://author:8080/author/" + strconv.Itoa(authorId)

    fmt.Println("Making call to>"+ url + "<")
    fmt.Println("bearer> '" + bearer + "' <")

    // make client
	superClient := http.Client{
		Timeout: time.Second * 2, // Maximum of 2 secs
	}

    // make request object
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
        fmt.Println("Unable to make new request to /author")
		return ""
	}

    // set headers
	req.Header.Set("User-Agent", "book-service-client")
	req.Header.Set("authorization", "Bearer " + bearer)

    // send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
        fmt.Println("Unable to send request to /author")
		return ""
	}

    // Check status code
    if ! strings.Contains(res.Status, "200")  {
        fmt.Println("Unable to connect to '" + url + "' to get names. HTTP code: " + res.Status)
        return ""
    }

    // parse body
	body, readErr := ioutil.ReadAll(res.Body)
	if readErr != nil {
        fmt.Println("Unable to parse response from /author")
		return ""
	}

    // get author info
	authorBean := author{}
	jsonErr := json.Unmarshal(body, &authorBean)
	if jsonErr != nil {
        fmt.Println("Unable to unmarshall response from /author")
		return ""
	}

    t := time.Now()
    elapsed := t.Sub(start)
    fmt.Println("Elapsed: ", elapsed)

    return authorBean.Name
}



////////////
// Split a CSV string into array
func splitCsvStringToArray(subjectCsv string) []string {
	subjects := strings.Split(subjectCsv, ",")
	return subjects
}

////////////
// Convert incoming int array to CSV string
func convertIntArrayToCsv(intArray []int) string {
    tempArray := make([]string, len(intArray))
    for i, v := range intArray {
        tempArray[i] = strconv.Itoa(v)
    }

    return strings.Join(tempArray, ",")
}
