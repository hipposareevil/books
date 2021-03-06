package main

// Tag service
// Supports:
// - DELETE at /tag/<tag_id> to delete a single 'tag'
// - PUT    at /tag/<tag_id> to update a 'tag'
// - GET    at /tag/<tag_id> to return a single 'tag'
// - GET    at /tag to return all 'tags'
// - POST   at /tag to create a new 'tag'

import (
	"fmt"
	"strings"

	"errors"

	"encoding/json"

	"database/sql"
	_ "github.com/go-sql-driver/mysql"
)

const TAG_CACHE = "tag"

// Service interface exposed to clients
type TagService interface {
	// GetTag: bearer, tag_id
	GetTag(string, int) (Tag, error)

	// Get tags: bearer, offset, limit
	GetTags(string, int, int) (Tags, error)

	// GetTag: bearer, tag_id
	DeleteTag(string, int) error

	// Create tag: bearer, tag name
	CreateTag(string, string) (Tag, error)

	// Update tag: bearer, tag name
	UpdateTag(string, string, int) error
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
// - cache layer
type tagService struct {
	mysqlDb *sql.DB
	cache   CacheLayer
}

//////////
// METHODS on tagService

////////////////
// Get tag
//
// params:
// bearer: Authorization bearer
// tagId : ID of tag to get
//
// returns:
// tag
// error
func (theService tagService) GetTag(bearer string, tagId int) (Tag, error) {
	fmt.Println(" ")
	fmt.Println("-- GetTag --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Tag{}, errors.New("unable to ping mysql")
	}

	// Make query
	var tag Tag
	// Scan the DB info into 'tag' variable
	err := theService.mysqlDb.QueryRow("SELECT tag_id, name FROM tag WHERE tag_id = ?", tagId).Scan(&tag.ID, &tag.Name)

	switch {
	case err == sql.ErrNoRows:
		return Tag{}, ErrNotFound
	case err != nil:
		fmt.Println("Got error from select: ", err)
		return Tag{}, ErrServerError
	}

	return tag, nil
}

////////////////
// Get tags
//
// params:
// bearer: authorization bearer
// offset : offset into list
// limit : number of items to get from list
//
// returns:
// tags
// error
func (theService tagService) GetTags(bearer string, offset int, limit int) (Tags, error) {
	fmt.Println(" ")
	fmt.Println("-- GetTags --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Tags{}, errors.New("unable to ping mysql")
	}

	// Get total number of rows
	var totalNumberOfRows int
	_ = theService.mysqlDb.QueryRow("SELECT COUNT(*) from tag").Scan(&totalNumberOfRows)

	if limit > totalNumberOfRows {
		limit = totalNumberOfRows
	}

	// Make query
	results, err := theService.mysqlDb.Query("SELECT tag_id, name FROM tag LIMIT ?,? ", offset, limit)
	if err != nil {
		return Tags{}, errors.New("unable to query mysql")
	}

	// slice of Tag entities
	datum := make([]Tag, 0, 0)

	// Parse results
	for results.Next() {
		var tag Tag
		// For each row, scan the result into our tag composite object:
		// tag_id, name
		err = results.Scan(&tag.ID, &tag.Name)
		if err != nil {
			return Tags{}, errors.New("unable to query mysql")
		}
		// and then print out the tag's Name attribute
		datum = append(datum, tag)
	}

	// Create Tags to return
	returnValue := Tags{
		Offset: offset,
		Limit:  limit,
		Total:  totalNumberOfRows,
		Data:   datum,
	}

	// Save all tags into cache if we have values
	if totalNumberOfRows > 0 {
		tagsAsBytes, err := json.Marshal(returnValue)
		if err == nil {
			// save to cache
			fmt.Println("Saving bytes to tag cache")
			go theService.cache.SetBytes(TAG_CACHE, 0, tagsAsBytes)
		} else {
			fmt.Println("Unable to save tags to cache:", err)
		}
	} else {
		fmt.Println("Not saving tags to cache as there are no datum yet")
	}

	return returnValue, nil
}

////////////////
// Delete tag
//
// params:
// bearer: Authorization bearer
// tagId : ID of tag to delete
//
// returns:
// error
func (theService tagService) DeleteTag(bearer string, tagId int) error {
	fmt.Println(" ")
	fmt.Println("-- DeleteTag --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql")
	}

	// Verify the tag exists, if not, throw ErrNotFound
	_, getErr := theService.GetTag(bearer, tagId)
	if getErr != nil {
		return getErr
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM tag WHERE tag_id = ?", tagId)

	// Delete from tagmapping as well.
	// Ignore the error for now.
	_, _ = theService.mysqlDb.Exec("DELETE FROM tagmapping WHERE tag_id = ?", tagId)

	// Delete the cache
	fmt.Println("Clearing TAG cache as we deleted a tag")
	theService.cache.Clear(TAG_CACHE, 0)

	return err
}

////////////////
// CreateTag
//
// params:
// bearer: authorization bearer
// name: name of new tag
//
// returns:
// tag
// error
func (theService tagService) CreateTag(bearer string, tagName string) (Tag, error) {
	fmt.Println(" ")
	fmt.Println("-- CreateTag --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return Tag{}, errors.New("unable to ping mysql")
	}

	// Make query
	stmt, err := theService.mysqlDb.Prepare("INSERT INTO tag SET name=?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return Tag{}, errors.New("Unable to prepare a DB statement: ")
	}

	res, err := stmt.Exec(tagName)
	if err != nil {
		fmt.Println("Error inserting into DB: ", err)
		if strings.Contains(err.Error(), "Duplicate entry ") {
			return Tag{}, ErrAlreadyExists
		} else {
			return Tag{}, errors.New("Unable to run INSERT against DB: ")
		}
	}

	// get the id
	id, _ := res.LastInsertId()

	// Create tag
	var tag Tag
	tag = Tag{
		ID:   int(id),
		Name: tagName,
	}

	// Delete the cache
	fmt.Println("Clearing TAG cache as we added a tag")
	theService.cache.Clear(TAG_CACHE, 0)

	return tag, nil
}

////////////////
// UpdateTag
//
// params:
// bearer: authorization bearer
// tagName: new name of tag
// tagId: id of tag to update
//
// returns:
// tag
// error
func (theService tagService) UpdateTag(bearer string, tagName string, tagId int) error {
	fmt.Println(" ")
	fmt.Println("--  CreateTag --")

	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		return errors.New("unable to ping mysql")
	}

	// Make query
	stmt, err := theService.mysqlDb.Prepare("UPDATE tag SET name=? WHERE tag_id = ?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return errors.New("Unable to prepare a DB statement: ")
	}

	// Delete the cache
	fmt.Println("Clearing TAG cache as we updated a tag")
	theService.cache.Clear(TAG_CACHE, 0)

	_, err = stmt.Exec(tagName, tagId)
	if err != nil {
		fmt.Println("Error updatingDB: ", err)
		if strings.Contains(err.Error(), "Duplicate entry ") {
			return ErrAlreadyExists
		} else {
			return errors.New("Unable to run update against DB: ")
		}
	}

	return nil
}
