package main

// Book service

import (
	_ "encoding/json"
	"fmt"
	_ "io/ioutil"
	_ "net/http"
	_ "strconv"
)

// Service interface exposed to clients
type ReviewService interface {
	GetReviews(string, int, int, int) (Reviews, error)
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
// - cache layer
type reviewService struct {
}

//////////
// METHODS on userbookService

////////////////
// Get Review
//
// returns:
// reviews
// error
func (theService reviewService) GetReviews(bearer string, offset int, limit int, bookId int) (Reviews, error) {
	fmt.Println("")
	fmt.Println("-- GetReviews --")

	var datum []Review

	// Query /user for list of users
	var users Users
	users, err := getUsers(bearer)
	if err != nil {
		fmt.Println("Unable to get users for reviews: ", err)
		return Reviews{}, ErrServerError
	}

	// For each user, query /user_book for reviews for the book
	for _, currentUser := range users.Data {
		fmt.Println("Found user: ", currentUser.Id)
		fmt.Println("Found user: ", currentUser.Name)

		userBook, err := getUserBook(bearer, currentUser.Id, bookId)

		if err == nil {
			// make a Review object for this user book
			newReview := Review{
				BookId:   bookId,
				Rating:   userBook.Rating,
				Tags:     userBook.Tags,
				UserName: currentUser.Name,
				Review:   userBook.Review,
			}
			datum = append(datum, newReview)
		} else {
			fmt.Println("Got error trying to get userbook for user: ", currentUser.Id, " :", err)
		}
	}

    ///////////////
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

	// Create Reviews to return
	returnValue := Reviews{
		Offset: offset,
		Limit:  realLimit,
		Total:  realNumberRows,
		Data:   datum,
	}

	return returnValue, nil
}
