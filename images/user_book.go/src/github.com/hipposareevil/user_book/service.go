package main

// Book service

import (
	"database/sql"
	_ "encoding/json"
	"errors"
	"fmt"
	"strings"
	"time"

	_ "github.com/go-sql-driver/mysql"
	_ "io/ioutil"
	_ "net/http"
	_ "strconv"
)

// Service interface exposed to clients
type UserBookService interface {
	// GetUserBooks:
	GetUserBooks(string, int, int, int, int, string, []string) (UserBooks, error)
	// GetUserBook:
	GetUserBook(string, int, int) (UserBook, error)

	// DeleteUserBook:
	DeleteUserBook(int, int) error

	// CreateUserBook (see createUserBookRequest for params)
	CreateUserBook(string, int, int, bool, []string, string) (UserBook, error)

	// UpdateUserBook
	// Same as CreateUserBook but the first param is the ID of book to update
	// first param: bearer
	UpdateUserBook(string, int, int, int, bool, *[]string, string) (UserBook, error)

	//////////////////////////////////////////////////

	// Helper method to get user books by filter
	getUserBooksByFilter(string, int, int, int, int, string, []string) (UserBooks, error)

	// Helper method to get all user books with no filters
	getAllUserBooks(string, int, int, int) (UserBooks, error)

	// get tag mappings for userbook
	getTagMappings(int, int) ([]string, error)

	// update tag mappings for userbook
	updateTagMappings(string, int, int, []string) ([]string, error)

	// delete tag mappings
	deleteTagMappings(int, int) error
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
// - cache layer
type userbookService struct {
	mysqlDb *sql.DB
	cache   CacheLayer
}

//////////
// METHODS on userbookService

////////////////
// Get UserBook
//
// returns:
// userbook
// error
func (theService userbookService) GetUserBook(bearer string, userId int, userBookId int) (UserBook, error) {
	fmt.Println("")
	fmt.Println("-- GetUserBook --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return UserBook{}, errors.New("unable to ping mysql")
	}

	// Make query
	var userBook UserBook

	// Scan the DB info into 'book' composite variable
	err := theService.mysqlDb.
		QueryRow("SELECT "+
        "user_book_id, user_id, book_id, rating, date_added, review "+
			"FROM userbook "+
			"WHERE user_id=? AND user_book_id=?",
			userId, userBookId).
                Scan(&userBook.UserBookId, &userBook.UserId, &userBook.BookId, &userBook.Rating, &userBook.DateAdded, &userBook.Review)

	switch {
	case err == sql.ErrNoRows:
		return UserBook{}, ErrNotFound
	case err != nil:
		fmt.Println("Got error from select: ", err)
		return UserBook{}, ErrServerError
	}

	//////////////////
	// get tag mappings
	var tags []string
	tags, err = theService.getTagMappings(userId, userBookId)
	if err != nil {
		fmt.Println("Unable to get tags for userbook: ", userBookId, " :", err)
		return UserBook{}, ErrServerError
	}

	// set tags
	userBook.Tags = tags

	//////////////////////////
	// Get book information
	err = getBookById(theService.cache, bearer, userBook.BookId, &userBook)
	if err != nil {
		fmt.Println("Error getting Book information for userbook ", userBookId)
		return UserBook{}, err
	}

	return userBook, nil
}

////////////////
// Get user books
//
// returns:
// books
// error
func (theService userbookService) GetUserBooks(bearer string, userId int, offset int, limit int, bookId int, title string, tags []string) (UserBooks, error) {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return UserBooks{}, errors.New("unable to ping mysql")
	}

	if len(title) > 0 ||
		len(tags) > 0 ||
		bookId > 0 {
		return theService.getUserBooksByFilter(bearer, userId, offset, limit, bookId, title, tags)
	} else {
		return theService.getAllUserBooks(bearer, userId, offset, limit)
	}
}

////////////////////
// Get all user books without query params besides offset/limit
//
func (theService userbookService) getAllUserBooks(bearer string, userId int, offset int, limit int) (UserBooks, error) {
	fmt.Println("")
	fmt.Println("-- GetUserBooks (all) --")

	// Get total number of rows
	var totalNumberOfRows int
	_ = theService.mysqlDb.QueryRow("SELECT COUNT(*) FROM userbook").Scan(&totalNumberOfRows)

	if limit > totalNumberOfRows {
		limit = totalNumberOfRows
	}

	// Make query
	results, err := theService.mysqlDb.
		Query("SELECT "+
        "user_book_id, user_id, book_id, rating, date_added, review "+
			"FROM userbook WHERE "+
			"user_id=? LIMIT ?,?", userId, offset, limit)

	if err != nil {
		fmt.Println("Got error from mysql: " + err.Error())
		return UserBooks{}, errors.New("unable to create query in mysql")
	}

	// slice of UserBook entities
	datum := make([]UserBook, 0, 0)

	// Parse results
	for results.Next() {
		var userBook UserBook

		// For each row, scan the result into our userbook composite object:
		err = results.
			Scan(&userBook.UserBookId, &userBook.UserId, &userBook.BookId, &userBook.Rating, &userBook.DateAdded, &userBook.Review)

		if err != nil {
			fmt.Println("Got error from mysql when getting all user books: " + err.Error())
			return UserBooks{}, errors.New("Unable to scan mysql for all user books.")
		}

		//////////////////
		// Get tag mappings
		var tags []string
		tags, err = theService.getTagMappings(userId, userBook.UserBookId)
		if err != nil {
			fmt.Println("Unable to get tags for userbook: ", userBook.UserBookId, " :", err)
			return UserBooks{}, ErrServerError
		}

		// set tags
		userBook.Tags = tags

		//////////////////////////
		// Get book information
		err = getBookById(theService.cache, bearer, userBook.BookId, &userBook)
		if err != nil {
			fmt.Println("Error getting Book information for userbook ", userBook.UserBookId)
			return UserBooks{}, err
		}

		datum = append(datum, userBook)
	}

	// Create Books to return
	returnValue := UserBooks{
		Offset: offset,
		Limit:  limit,
		Total:  totalNumberOfRows,
		Data:   datum,
	}

	return returnValue, nil
}

///////////////////////
// Get user books by filter (query param)
//
func (theService userbookService) getUserBooksByFilter(bearer string, userId int, offset int, limit int,
	desiredBookId int, desiredTitle string, desiredTags []string) (UserBooks, error) {
	fmt.Println("")
	fmt.Println("-- GetUserBooks (by filter) --")

	/////////////////////
	// Query for each portion of the filter:
	// - tags
	// - title
	// - bookId

    // full set of userBook IDs to return
    finalUserBookIds := []int{}

    userBookIdsInTagBucket := []int{}
    userBookIdsInTitleBucket := []int{}
    userBookIdsInIdBucket := []int{}

	//////////////////
	// #1: by Tag

	// tags index by the name
	var tagsInDatabase map[string]Tag
	allTags := getAllTags(theService.cache, bearer)
	tagsInDatabase = convertTagsJsonToArray(allTags)

	// Loop through all tags and get userbooks for those
	// tags. We throw out invalid desiredTags along the way
	var tagIds []int
	for _, currentTag := range desiredTags {
		if tag, ok := tagsInDatabase[currentTag]; ok {
			// tag is in the database
			tagIds = append(tagIds, tag.ID)
		}
	}

	// Only query for tags if we have some to look for
	if len(tagIds) > 0 {
		// Get all user_book IDs for the tags

        selectString := "SELECT user_book_id FROM tagmapping WHERE " +
            "tag_id IN (" + convertIntArrayToCsv(tagIds) + ") " +
            " AND user_id=?"

		results, err := theService.mysqlDb.Query(selectString,
            userId)

		// Parse results
		for results.Next() {
			// scan the id
			var userBookId int

			err = results.Scan(&userBookId)
			if err != nil {
				return UserBooks{}, errors.New("Unable to scan userbook (by tag): " + err.Error())
			}

            userBookIdsInTagBucket = append(userBookIdsInTagBucket, userBookId)
		}

        finalUserBookIds = userBookIdsInTagBucket
	}

	////////////////
	// #2: by title

	// Get list of books for the incoming title
    desiredTitle = strings.TrimSpace(desiredTitle)
    // list of book ids we want
    var bookIds []int

    if len(desiredTitle) > 0 {
        var booksByTitle Books
        booksByTitle, err := getBooksByTitle(bearer, desiredTitle)
        if err != nil {
            return UserBooks{}, ErrServerError
        }

        for _, currentBook := range booksByTitle.Data {
            // Get user book ID for this bookid
            bookIds = append(bookIds, currentBook.Id)
        }
    }


	// only look for book ids if we have some to scan
	if len(bookIds) > 0 {
        bookIdsAsString := convertIntArrayToCsv(bookIds)

		// Get all user_book IDs for these book ids
        selectString := "SELECT user_book_id FROM userbook WHERE book_id in (" +
            bookIdsAsString + ") AND user_id=?"

		results, err := theService.mysqlDb.
            Query(selectString, userId)

		if err != nil {
			return UserBooks{}, ErrServerError
		}

		// Parse results
		for results.Next() {
			// scan the id
			var userBookId int

			err = results.Scan(&userBookId)
			if err != nil {
				return UserBooks{}, errors.New("Unable to scan userbook (by title): " + err.Error())
			}
            userBookIdsInTitleBucket = append(userBookIdsInTitleBucket, userBookId)
		}

        finalUserBookIds = userBookIdsInTitleBucket

        // update final set
        if len(tagIds) > 0 {
            // if tags were queried for, union them with the final set
            finalUserBookIds = Union(finalUserBookIds, userBookIdsInTagBucket)
        }
	}


    ///////////////////
	// #3: by book_id
	if (desiredBookId > 0) {
		// Get all user_book IDs for these book ids
        selectString := "SELECT user_book_id FROM userbook WHERE book_id =? AND user_id=?"

		results, err := theService.mysqlDb.
            Query(selectString, desiredBookId, userId)

		if err != nil {
			fmt.Println("Got error from mysql when getting book ids: " + err.Error())
			return UserBooks{}, ErrServerError
		}

		// Parse results
		for results.Next() {
			// scan the id
			var userBookId int

			err = results.Scan(&userBookId)
			if err != nil {
				return UserBooks{}, errors.New("Unable to scan userbook (by title): " + err.Error())
			}
            
            userBookIdsInIdBucket = append(userBookIdsInIdBucket, userBookId)
		}

        finalUserBookIds = userBookIdsInIdBucket

        // update final set
        if len(tagIds) > 0 {
            // if tags were queried for, union them with the final set
            finalUserBookIds = Union(finalUserBookIds, userBookIdsInTagBucket)
        }        
        if len(desiredTitle) > 0 {
            // if title was queried for, union that with the final set
            finalUserBookIds = Union(finalUserBookIds, userBookIdsInTitleBucket)
        }        
    }

	////////////////////////
	// Now for each user book id, get the real user books

	// slice of UserBook entities
	datum := make([]UserBook, 0, len(finalUserBookIds))

	// Only query if there are some IDs to search for
	if len(finalUserBookIds) > 0 {
        selectString := "SELECT "+
            "user_book_id, user_id, book_id, rating, date_added, review "+
				"FROM userbook WHERE "+
				"user_book_id in (" + convertIntArrayToCsv(finalUserBookIds) + ")"

		// Make query
		results, err := theService.mysqlDb.Query(selectString)

		if err != nil {
			fmt.Println("Got error from mysql when getting all user book ids : " + err.Error())
			return UserBooks{}, errors.New("unable to create query in mysql")
		}

		// Parse results
		for results.Next() {
			var userBook UserBook

			// For each row, scan the result into our userbook composite object:
			err = results.
				Scan(&userBook.UserBookId, &userBook.UserId, &userBook.BookId, &userBook.Rating, &userBook.DateAdded, &userBook.Review)

			if err != nil {
				fmt.Println("Got error from mysql when getting all user books: " + err.Error())
				return UserBooks{}, errors.New("Unable to scan mysql for all user books.")
			}

			//////////////////
			// Get tag mappings
			var tags []string
			tags, err = theService.getTagMappings(userId, userBook.UserBookId)
			if err != nil {
				fmt.Println("Unable to get tags for userbook: ", userBook.UserBookId, " :", err)
				return UserBooks{}, ErrServerError
			}

			// set tags
			userBook.Tags = tags

			//////////////////////////
			// Get book information
			err = getBookById(theService.cache, bearer, userBook.BookId, &userBook)
			if err != nil {
				fmt.Println("Error getting Book information for userbook ", userBook.UserBookId)
				return UserBooks{}, err
			}

			datum = append(datum, userBook)
		}
	}

    //////////////
    // Update return data
    
    // Get the total number of rows
    realNumberRows := len(datum)
    realLimit := limit

    // fix offset
    if (offset > realNumberRows) || (offset < 0) {
        offset = 0        
    }

    // fix limit
    if (realLimit < 0) {
        realLimit = len(datum)
    }

    if (realLimit > realNumberRows) {
        realLimit = realNumberRows
    }

    // determine slice of datum to use
    whereToEnd := offset + realLimit
    if (whereToEnd > realNumberRows) {
        whereToEnd = realNumberRows
    }

    datum = datum[offset:whereToEnd]

	// Create Books to return
	returnValue := UserBooks{
		Offset: offset,
		Limit:  realLimit,
		Total:  realNumberRows,
		Data:   datum,
	}

	return returnValue, nil
}

////////////////
// Delete user book
//
// returns:
// error
func (theService userbookService) DeleteUserBook(userId int, userBookId int) error {
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql when deleting userbook")
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM userbook WHERE user_id = ? AND user_book_id = ?",
		userId, userBookId)

	return err
}

////////////////
// CreateUserBook
//
// returns:
// userbook
// error
func (theService userbookService) CreateUserBook(bearer string, userId int, bookId int, rating bool, incomingTags []string, review string) (UserBook, error) {

	fmt.Println("")
	fmt.Println("-- CreateUserBook --")

	////////////////////
	// verify mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return UserBook{}, errors.New("unable to ping mysql")
	}

	// Make prepared insert statement
	stmt, err := theService.mysqlDb.
		Prepare("INSERT INTO userbook SET " +
			"user_id=?, book_id=?, " +
			"rating=?, date_added=?, review=? ")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return UserBook{}, errors.New("Unable to prepare a DB statement: ")
	}

	// now for 'date_added'
	now := time.Now()

	///////////////////////
	// Execute Insert
	res, err := stmt.Exec(userId, bookId, rating, now, review)

	if err != nil {
		fmt.Println("Error inserting into userbook DB: ", err)
		if strings.Contains(err.Error(), "Duplicate entry ") {
			return UserBook{}, ErrAlreadyExists
		} else {
			return UserBook{}, errors.New("Unable to run INSERT against userbook DB: ")
		}
	}

	// get the id
	userBookId, _ := res.LastInsertId()

	///////////////////////
	// Update the tag mappings
	tagNamesAdded, err := theService.updateTagMappings(bearer, userId, int(userBookId), incomingTags)
	if err != nil {
		fmt.Println("Error updating tag mappings for new userbook. Bookid: ", bookId)
		return UserBook{}, err
	}

	var userBookToReturn UserBook
	userBookToReturn = UserBook{
		BookId:     bookId,
		Rating:     rating,
		Tags:       tagNamesAdded,
		UserId:     userId,
		UserBookId: int(userBookId),
        Review: review,
		DateAdded:  now,
	}

	//////////////////////////
	// Get book information
	err = getBookById(theService.cache, bearer, bookId, &userBookToReturn)
	if err != nil {
		fmt.Println("Error getting Book information for new userbook. book id: ", bookId)
		return UserBook{}, err
	}

	return userBookToReturn, nil
}

////////////////
// UpdateUserBook
//
// returns:
// error
func (theService userbookService) UpdateUserBook(bearer string, userId int, userBookId int, bookId int, rating bool, incomingTags *[]string, review string) (UserBook, error) {
	fmt.Println("")
	fmt.Println("-- UpdateUserBook --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return UserBook{}, errors.New("unable to ping mysql")
	}

	// Make query
	stmt, err := theService.mysqlDb.
		Prepare("UPDATE userbook SET " +
			"book_id=COALESCE(NULLIF(?,''),book_id), " +
			"review=COALESCE(NULLIF(?,''),review), " +
			"rating=? " + 
			"WHERE user_id=? AND user_book_id=?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB when updating userbook: ", err)
		return UserBook{}, errors.New("Unable to prepare a DB statement when updating userbook: ")
	}

	_, err = stmt.Exec(bookId, review, rating, userId, userBookId)

	if err != nil {
		fmt.Println("Error updating DB for userbook: ", err)
		return UserBook{}, errors.New("Unable to run update against DB for userbook: ")
	}

	//////////////////////
	// Clear old tag mappings and add new ones

    if incomingTags != nil {
        err = theService.deleteTagMappings(userId, userBookId)
        if err != nil {
            fmt.Println("Error updating tag mappings for updated userbook. UserBookId: ", userBookId)
            return UserBook{}, err
        }

        _, err = theService.updateTagMappings(bearer, userId, int(userBookId), *incomingTags)
        if err != nil {
            fmt.Println("Error updating tag mappings for updated userbook . userBookid: ", userBookId)
            return UserBook{}, err
        }
    }

    ///////////////
    // Get full userbook
    fmt.Println("Get updated user book for user:",userId, " and book id:", userBookId)
    userBookToReturn, err := theService.GetUserBook(bearer, userId, userBookId)

	return userBookToReturn, nil
}

/////////////////
// Get tag names for a userbook
//
// returns:
// array of tag names for the incoming userbook
func (theService userbookService) getTagMappings(userId int, userBookId int) ([]string, error) {
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return nil, errors.New("unable to ping mysql")
	}

	var tagsToReturn []string
    tagsToReturn = make([]string, 0)

	// make query
	results, err := theService.mysqlDb.
		Query("SELECT T.name "+
			"FROM tag AS T, tagmapping AS M "+
			"WHERE M.user_id=? AND M.user_book_id=? AND "+
			"M.tag_id = T.tag_id",
			userId, userBookId)

	// Parse results
	for results.Next() {
		var currentTitle string

		// For each row, scan the result into our book composite object:
		err = results.Scan(&currentTitle)

		if err != nil {
			fmt.Println("Got error from mysql when getting tags for userbook: " + err.Error())
			return tagsToReturn, errors.New("Unable to scan mysql for all user Book tags.")
		}

		tagsToReturn = append(tagsToReturn, currentTitle)
	}

	return tagsToReturn, nil
}

/////////////
// Update the tag mappings
//
// returns:
// array of tag names that were added
func (theService userbookService) updateTagMappings(bearer string, userId int, userBookId int, incomingTags []string) ([]string, error) {
	// all tags in database, queried from /tag endpoint
	allTags := getAllTags(theService.cache, bearer)
	tagsInDatabase := convertTagsJsonToArray(allTags)
	// map of tags to add for this user book mapping
	tagsToAddToMapping := make(map[string]Tag)

	// loop through names of 'tags to add' and put them
	// into the tagsToAddMapping.
	for _, tagToAdd := range incomingTags {
		// See if tagToAdd is a valid tag, and if so,
		// add a mapping with that id
		if tag, ok := tagsInDatabase[tagToAdd]; ok {
			// tag is in DB
			tagsToAddToMapping[tag.Name] = tag
		} else {
			fmt.Println("Not adding tag ", tagToAdd, " as it doesn't exist in DB")
		}
	}

	// keep list of tag names that were actually added
	tagNamesAdded := []string{}

	// Loop through tags to add
	for tagName, currentTag := range tagsToAddToMapping {
		tagId := currentTag.ID

		stmt, err := theService.mysqlDb.
			Prepare("INSERT INTO tagmapping SET " +
				"user_book_id=?, user_id=?, tag_id=?")
		defer stmt.Close()
		if err != nil {
			fmt.Println("Error preparing DB for adding tag mappings: ", err)
			return nil, errors.New("Unable to prepare a DB statement: ")
		}

		// Execute statement
		_, err = stmt.Exec(userBookId, userId, tagId)

		if err != nil {
			fmt.Println("Error inserting into tagmapping DB: ", err)
			if strings.Contains(err.Error(), "Duplicate entry ") {
				return nil, ErrAlreadyExists
			} else {
				return nil, errors.New("Unable to run INSERT against tagmapping DB")
			}
		}

		fmt.Println("Added tagmapping for tag: ", tagName, ":", tagId)
		tagNamesAdded = append(tagNamesAdded, tagName)
	}

	return tagNamesAdded, nil
}

/////////////
// Delete the tag mappings for a user book
//
func (theService userbookService) deleteTagMappings(userId int, userBookId int) error {
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql")
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM tagmapping WHERE user_id=? and user_book_id=?", userId, userBookId)

	return err
}
