package main

// Book service

import (
	"database/sql"
	"errors"
	"fmt"
    "encoding/json"
	_ "github.com/go-sql-driver/mysql"
	"strings"
)

// namespace for author cache, this will be indexed by author id
const AUTHOR_CACHE = "author.name"
// namespace for this book cache, this will be indexed by book id
const BOOK_CACHE = "book.info"

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
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
type bookService struct {
	mysqlDb *sql.DB
	cache   CacheLayer
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
		QueryRow("SELECT "+
			"book_id, author_id, year, title, "+
			"isbn, subjects, ol_works, description, "+
			"image_small, image_medium, image_large, goodreads_url "+
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
		book.AuthorName = getAuthorNameById(theService.cache, bearer, book.AuthorId)
	}

	// Convert subjects from CSV to string array
	book.Subjects = splitCsvStringToArray(subjectAsCsv)

	// Convert isbns from CSV to string array
	book.Isbns = splitCsvStringToArray(isbnsAsCsv)

    // Save to cache
    bookAsBytes, err := json.Marshal(book)
    if err == nil {
        fmt.Println("Saving book to book cache")
        go theService.cache.SetBytes(BOOK_CACHE, bookId, bookAsBytes)
    } else {
        fmt.Println("Unable to save book to cache:", err)
    }


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

    var appendedString string


	// Title
	if len(title) > 0 {
		updated = true
		appendedString = "WHERE title LIKE '%" + title + "%' "
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
        appendedString += prependValue + " author_id in (" + authorIdsAsCsv + ")"
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
        appendedString += prependValue + " book_id in (" + bookIdsAsCsv + ")"

		updated = true
	}

    // Re get total # of rows for the return value
    countQuery := "SELECT COUNT(*) FROM book " + appendedString;
    fmt.Println("get count query with: ", countQuery)
	_ = theService.mysqlDb.QueryRow(countQuery).Scan(&totalNumberOfRows)

    // real select string
    selectString = selectString + appendedString
	fmt.Println("Making query with: " + selectString)

    // Make query
	results, err := theService.mysqlDb.
		Query(selectString+
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

		fmt.Println("Get author name for book with id: ", book.Id)
		fmt.Println("Get author name for book with author id: ", book.AuthorId)

		// Get the author name
		book.AuthorName = getAuthorNameById(theService.cache, bearer, book.AuthorId)

		// Convert subjects from CSV to string array
		book.Subjects = splitCsvStringToArray(subjectAsCsv)

		// Convert isbns from CSV to string array
		book.Isbns = splitCsvStringToArray(isbnsAsCsv)

		datum = append(datum, book)

        // Save to cache
        bookAsBytes, err := json.Marshal(book)
        if err == nil {
            fmt.Println("Saving book to book cache")
            go theService.cache.SetBytes(BOOK_CACHE, book.Id, bookAsBytes)
        } else {
            fmt.Println("Unable to save book to cache:", err)
        }
	}

    // reset the limit (number of things being returned)
    limit = len(datum)

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
    // first param is empty for the bearer as we don't need to get the extra info
	_, getErr := theService.GetBook("", bookId)
	if getErr != nil {
		return getErr
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM book WHERE book_id = ?", bookId)

    // remove from cache
    theService.cache.Clear(BOOK_CACHE, bookId)

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
	authorId int,
	description string,
	firstPublishedYear int,
	goodReadsUrl string,
	imageLarge string,
	imageMedium string,
	imageSmall string,
	isbns []string,
	openlibraryWorkUrl string,
	subjects []string,
	title string) (Book, error) {

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

    fmt.Println("ISBNS ", len(isbns))
    fmt.Println("ISBNS ", isbns)

	// Make prepared insert statement
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

	// get the book back
	bookToReturn, err := theService.GetBook(bearer, int(id))

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
	authorId int,
	description string,
	firstPublishedYear int,
	goodReadsUrl string,
	imageLarge string,
	imageMedium string,
	imageSmall string,
	isbns []string,
	openlibraryWorkUrl string,
	subjects []string,
	title string) (Book, error) {

	fmt.Println("")
	fmt.Println("-- UpdateBook --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Book{}, errors.New("unable to ping mysql")
	}

	fmt.Println("Updating book by id: ", bookId)

	// Make query
	stmt, err := theService.mysqlDb.
		Prepare("UPDATE book SET " +
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

	_, err = stmt.Exec(authorId, firstPublishedYear, title,
		isbnsAsCsv, subjectsAsCsv, openlibraryWorkUrl, goodReadsUrl, description,
		imageSmall, imageMedium, imageLarge, bookId)

	if err != nil {
		fmt.Println("Error updatingDB for book: ", err)
		return Book{}, errors.New("Unable to run update against DB for book: ")
	}

    // This next call will save the book into the cache and overwrite existing data

	// get the book back
	bookToReturn, err := theService.GetBook(bearer, bookId)

	return bookToReturn, err
}

