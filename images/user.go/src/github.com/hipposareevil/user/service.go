package main

// User service

import (
	"fmt"
	"strings"

	"errors"

	"database/sql"
	_ "github.com/go-sql-driver/mysql"

	// password encryption
	"golang.org/x/crypto/bcrypt"
)

// Service interface exposed to clients
type UserService interface {
	// GetUsers: offset, limit
	GetUsers(int, int) (Users, error)

	// GetUser: id
	GetUser(int) (User, error)

	// DeleteUser: id
	DeleteUser(int) error

	// CreateUser
	// name, usergroup, data, password
	CreateUser(string, string, string, string) (User, error)

	// UpdateUser
	// id, name, usergroup, data, password
	UpdateUser(int, string, string, string, string) error
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
type userService struct {
	mysqlDb *sql.DB
}

//////////
// METHODS on userService

////////////////
// Get User
//
// params:
// bearer: Authorization bearer
// userId : ID of user to get
//
// returns:
// user
// error
func (theService userService) GetUser(userId int) (User, error) {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
        fmt.Println("got ping error: ", err)
		return User{}, errors.New("unable to ping mysql")
	}

	// Make query
	var user User
	// Scan the DB info into 'user' variable
	err := theService.mysqlDb.
		QueryRow("SELECT user_id, name, user_group, data FROM user WHERE user_id = ?", userId).
		Scan(&user.Id, &user.Name, &user.UserGroup, &user.Data)

	switch {
	case err == sql.ErrNoRows:
		return User{}, ErrNotFound
	case err != nil:
		fmt.Println("Got error from select: ", err)
		return User{}, ErrServerError
	default:
		fmt.Println("got user!", user)
	}

	return user, nil
}

////////////////
// Get users
//
// params:
// offset : offset into list
// limit : number of items to get from list
//
// returns:
// users
// error
func (theService userService) GetUsers(offset int, limit int) (Users, error) {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
        fmt.Println("got ping error: ", err)
		return Users{}, errors.New("unable to ping mysql")
	}

	// Get total number of rows
	var totalNumberOfRows int
	_ = theService.mysqlDb.QueryRow("SELECT COUNT(*) from user").Scan(&totalNumberOfRows)

	if limit > totalNumberOfRows {
		limit = totalNumberOfRows
	}

	// Make query
	results, err := theService.mysqlDb.
		Query("SELECT user_id, name, user_group, data FROM user LIMIT ?,? ", offset, limit)
	if err != nil {
		fmt.Println("Got error from mysql: " + err.Error())
		return Users{}, errors.New("unable to query mysql")
	}

	// slice of User entities
	datum := make([]User, 0, 0)

	// Parse results
	for results.Next() {
		var user User
		// For each row, scan the result into our user composite object:
		// user_id, name
		err = results.Scan(&user.Id, &user.Name, &user.UserGroup, &user.Data)
		if err != nil {
			fmt.Println("Got error from mysql: " + err.Error())
			return Users{}, errors.New("Unable to query mysql")
		}
		datum = append(datum, user)
	}

	// Create Users to return
	returnValue := Users{
		Offset: offset,
		Limit:  limit,
		Total:  totalNumberOfRows,
		Data:   datum,
	}

	return returnValue, nil
}

////////////////
// Delete user
//
// params:
// userId : ID of user to delete
//
// returns:
// error
func (theService userService) DeleteUser(userId int) error {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
        fmt.Println("got ping error: ", err)
		return errors.New("unable to ping mysql")
	}

	// Verify the user exists, if not, throw ErrNotFound
	_, getErr := theService.GetUser(userId)
	if getErr != nil {
		return getErr
	}

	// Make DELETE query
	_, err := theService.mysqlDb.Exec("DELETE FROM user WHERE user_id = ?", userId)

	return err
}

////////////////
// CreateUser
//
// params:
// name: name of new user
// userGroup
// data
// password (unencrypted at this point)
//
// returns:
// user
// error
func (theService userService) CreateUser(userName string, userGroup string, data string, password string) (User, error) {
	////////////////////
	// verify mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
        fmt.Println("got ping error: ", err)
		return User{}, errors.New("unable to ping mysql")
	}

	// encrypt password
	encryptedPassword, err := encrypt(password)
	if err != nil {
		fmt.Println("Unable to encrypt password")
		return User{}, errors.New("Unable to encrypt password")
	}

	// Make insert
	stmt, err := theService.mysqlDb.
		Prepare("INSERT INTO user SET name=?, user_group=?, data=?, password=?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return User{}, errors.New("Unable to prepare a DB statement: ")
	}

	res, err := stmt.Exec(userName, userGroup, data, encryptedPassword)
	if err != nil {
		fmt.Println("Error inserting into DB: ", err)
		if strings.Contains(err.Error(), "Duplicate entry ") {
			return User{}, ErrAlreadyExists
		} else {
			return User{}, errors.New("Unable to run INSERT against DB: ")
		}
	}

	// get the id
	id, _ := res.LastInsertId()

	// Create user
	var user User
	user = User{
		Id:        int(id),
		Name:      userName,
		Data:      data,
		UserGroup: userGroup,
	}

	return user, nil
}

////////////////
// UpdateUser
//
// params:
// userId: id of user to update
// userName: new name of user
// userGroup
// data
// password (unencrypted at this point)
//
// returns:
// error
func (theService userService) UpdateUser(userId int, userName string, userGroup string, data string, password string) error {
	////////////////////
	// Get data from mysql
	// mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
        fmt.Println("got ping error: ", err)
		return errors.New("unable to ping mysql")
	}

	// encrypt password
    encryptedPassword, err := encrypt(password)
    if err != nil {
        return errors.New("Unable to encrypt password")
    }

    // change empty to null
    if len(password) <= 0 {
        encryptedPassword = ""
    }

	// Make query
	stmt, err := theService.mysqlDb.
		Prepare("UPDATE user SET " +
        "name=COALESCE(NULLIF(?,''),name), " +
        "data=COALESCE(NULLIF(?,''),data), " +
        "user_group=COALESCE(NULLIF(?,''),user_group), " +
        "password=COALESCE(NULLIF(?,''),password) " + 
        "WHERE user_id = ?")
	defer stmt.Close()
	if err != nil {
		fmt.Println("Error preparing DB: ", err)
		return errors.New("Unable to prepare a DB statement: ")
	}

	_, err = stmt.Exec(userName, data, userGroup, encryptedPassword, userId)
	if err != nil {
		fmt.Println("Error updatingDB: ", err)
		return errors.New("Unable to run update against DB: ")
	}

	return nil
}

// Encrypt the incoming plaintext password.
// Returns a new string w/ encrypted text.
func encrypt(password string) (string, error) {
	asByte := []byte(password)

	encrypted, err := bcrypt.GenerateFromPassword(asByte, bcrypt.MinCost)

	if err != nil {
		fmt.Println("Error encrypting password.")
		fmt.Println(err)
		return "", err
	}

	encryptedPassword := string(encrypted[:])

	return encryptedPassword, nil
}
