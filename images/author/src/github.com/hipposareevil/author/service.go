package main

// Author service

import (
	"fmt"
	"strings"

	"errors"

	"database/sql"
	_ "github.com/go-sql-driver/mysql"
)

const AUTHOR_CACHE="author.name"

// Service interface exposed to clients
type AuthorService interface {
	// GetAuthors: offset, limit
	GetAuthors(int, int, string) (Authors, error)

	// GetAuthor: id
	GetAuthor(int) (Author, error)

	// DeleteAuthor: id
	DeleteAuthor(int) error

	// CreateAuthor
	// name, authorgroup, data, password
	CreateAuthor(string, string, string, string, string, string, string, []string) (Author, error)

	// UpdateAuthor
	// id, name, authorgroup, data, password
	UpdateAuthor(int, string, string, string, string, string, string, string, []string) error
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
type authorService struct {
	mysqlDb *sql.DB
    cache CacheLayer
}

//////////
// METHODS on authorService

////////////////
// Get Author
//
// params:
// bearer: Authorization bearer
// authorId : ID of author to get
//
// returns:
// author
// error
func (theService authorService) GetAuthor(authorId int) (Author, error) {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Author{}, errors.New("unable to ping mysql")
	}

	// Make query
	var author Author
	var subjectAsCsv string

	// Scan the DB info into 'author' composite variable
	err := theService.mysqlDb.
		QueryRow("SELECT author_id, name, birth_date, "+
        "image_small, image_medium, image_large, "+
        "ol_key, goodreads_url, subjects "+
        "FROM author WHERE author_id = ?", authorId).
		Scan(&author.Id, &author.Name, &author.BirthDate,
        &author.ImageSmall, &author.ImageMedium, &author.ImageLarge,
        &author.OlKey, &author.GoodReadsUrl, &subjectAsCsv)

	switch {
	case err == sql.ErrNoRows:
		return Author{}, ErrNotFound
	case err != nil:
		fmt.Println("Got error from select: ", err)
		return Author{}, ErrServerError
	default:
		fmt.Println("got author")
	}

	// Convert subjects from CSV to string array
	author.Subjects = splitCsvStringToArray(subjectAsCsv)

    // Cache author name by id
    go theService.cache.Set(AUTHOR_CACHE, authorId, author.Name)

	return author, nil
}

////////////////
// Get authors
//
// params:
// offset : offset into list
// limit : number of items to get from list
// name : partial name of author
//
// returns:
// authors
// error
func (theService authorService) GetAuthors(offset int, limit int, name string) (Authors, error) {
    fmt.Println("")
    fmt.Println("-- GetAuthors --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Authors{}, errors.New("unable to ping mysql")
	}

	// Get total number of rows
	var totalNumberOfRows int
    countQuery := "SELECT COUNT(*) FROM author "
	_ = theService.mysqlDb.QueryRow(countQuery).
        Scan(&totalNumberOfRows)

	if limit > totalNumberOfRows {
		limit = totalNumberOfRows
	}

	fmt.Println("Looking for author with limit ", limit)
	fmt.Println("Looking for author with offset ", offset)

	// Create SELECT string
	selectString := "SELECT author_id, name, birth_date, " +
		"image_small, image_medium, image_large, " +
		"ol_key, goodreads_url, subjects " +
		"FROM author "

	// Make query
    if len(name) > 0 {
        // Update query to add 'name'
        appendString :=" WHERE name LIKE '%" + name + "%' "
        fmt.Println("Looking for author with name like '" + name + "'")

        selectString += appendString
        countQuery += appendString
    }


    // Redo the total number of rows
    _ = theService.mysqlDb.QueryRow(countQuery).Scan(&totalNumberOfRows)

    // Make query
   results, err := theService.mysqlDb.Query(
       selectString + "LIMIT ?,?",
       offset, limit)

	if err != nil {
		fmt.Println("Got error from mysql when querying for all authors: " + err.Error())
		return Authors{}, errors.New("unable to create query in mysql")
	}

	// slice of Author entities
	datum := make([]Author, 0, 0)

    // Make hashmap of author ID to author name for the cache
    kvMap := make(map[int]string)

	// Parse results
	for results.Next() {
		var subjectAsCsv string
		var author Author

		// For each row, scan the result into our author composite object:
		err = results.Scan(&author.Id, &author.Name, &author.BirthDate,
			&author.ImageSmall, &author.ImageMedium, &author.ImageLarge,
			&author.OlKey, &author.GoodReadsUrl, &subjectAsCsv)

		if err != nil {
			fmt.Println("Got error from mysql: " + err.Error())
			return Authors{}, errors.New("Unable to scan the query mysql")
		}

		// Convert subjects from CSV to string array
		author.Subjects = splitCsvStringToArray(subjectAsCsv)

        // Save the authors name indexed by id
        kvMap[author.Id] = author.Name

		datum = append(datum, author)
	}

    // Cache author name by id for all authors found
    go theService.cache.SetMultiple(AUTHOR_CACHE, kvMap)

    // reset the limit (number of things being returned)
    limit = len(datum)

	// Create Authors to return
	returnValue := Authors{
		Offset: offset,
		Limit:  limit,
		Total:  totalNumberOfRows,
		Data:   datum,
	}

	return returnValue, nil
}

////////////////
// Delete author
//
// params:
// authorId : ID of author to delete
//
// returns:
// error
func (theService authorService) DeleteAuthor(authorId int) error {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql")
	}

	// Verify the author exists, if not, throw ErrNotFound
	_, getErr := theService.GetAuthor(authorId)
	if getErr != nil {
		return getErr
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM author WHERE author_id = ?", authorId)

    // Clear cache for this author
    theService.cache.Clear(AUTHOR_CACHE, authorId)

	return err
}

////////////////
// CreateAuthor
//
// returns:
// author
// error
func (theService authorService) CreateAuthor(authorName string,
	birthDate string,
	olKey string,
	goodreadsUrl string,
	imageSmall string,
	imageMedium string,
	imageLarge string,
	subjects []string) (Author, error) {

	////////////////////
	// verify mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Author{}, errors.New("unable to ping mysql")
	}

	// Convert []string to string as csv for database
	subjectsAsCsv := strings.Join(subjects[:], ",")

	// Make insert
	stmt, err := theService.mysqlDb.
		Prepare("INSERT INTO author SET " +
			"name=?, birth_date=?, subjects=?, image_small=?, image_medium=?, image_large=?, ol_key=?, goodreads_url=?")

	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return Author{}, errors.New("Unable to prepare a DB statement: ")
	}

	res, err := stmt.Exec(authorName, birthDate, subjectsAsCsv, imageSmall, imageMedium, imageLarge, olKey, goodreadsUrl)
	if err != nil {
		fmt.Println("Error inserting into DB: ", err)
		if strings.Contains(err.Error(), "Duplicate entry ") {
			return Author{}, ErrAlreadyExists
		} else {
			return Author{}, errors.New("Unable to run INSERT against DB: ")
		}
	}

	// get the id
	id, _ := res.LastInsertId()

    author, err := theService.GetAuthor(int(id))

	return author, err
}

////////////////
// UpdateAuthor
//
// returns:
// error
func (theService authorService) UpdateAuthor(authorId int,
	authorName string,
	birthDate string,
	olKey string,
	goodreadsUrl string,
	imageSmall string,
	imageMedium string,
	imageLarge string,
	subjects []string) error {

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql")
	}

	fmt.Println("Updating author by iD: ", authorId)

	// Make query
	stmt, err := theService.mysqlDb.
		Prepare("UPDATE author SET " +
			"name=COALESCE(NULLIF(?,''),name), " +
			"birth_date=COALESCE(NULLIF(?,''),birth_date), " +
			"subjects=COALESCE(NULLIF(?,''),subjects), " +
			"image_small=COALESCE(NULLIF(?,''),image_small), " +
			"image_medium=COALESCE(NULLIF(?,''),image_medium), " +
			"image_large=COALESCE(NULLIF(?,''),image_large), " +
			"ol_key=COALESCE(NULLIF(?,''),ol_key), " +
			"goodreads_url=COALESCE(NULLIF(?,''),goodreads_url) " +
			"WHERE author_id = ?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return errors.New("Unable to prepare a DB statement: ")
	}

	// Convert []string to string as csv for database
	subjectsAsCsv := strings.Join(subjects[:], ",")

	_, err = stmt.Exec(authorName, birthDate, subjectsAsCsv, imageSmall, imageMedium, imageLarge, olKey, goodreadsUrl, authorId)
	if err != nil {
		fmt.Println("Error updatingDB: ", err)
		return errors.New("Unable to run update against DB: ")
	}

    // Clear cache for this author
    theService.cache.Clear(AUTHOR_CACHE, authorId)

	return nil
}



////////////
// Split a CSV string into array
func splitCsvStringToArray(subjectCsv string) []string {
    if len(subjectCsv) > 0 {
        return strings.Split(subjectCsv, ",")
    } else {
        return make([]string, 0)
    }
}
