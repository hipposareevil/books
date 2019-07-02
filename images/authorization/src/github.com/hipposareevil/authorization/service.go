package main

/////////////////////////
// Authorize service
// Supports:
// - GET    at /authorize to validate authorization header  (200 or 401)
// - POST   at /authorize to create a new token
//
/////////////////////////

import (
	"fmt"

	// password encryption
	"golang.org/x/crypto/bcrypt"

	// UUID
	"github.com/satori/go.uuid"

	_ "errors"

	// redis
	"github.com/mediocregopher/radix.v2/pool"

	// mysql
	"database/sql"
	_ "github.com/go-sql-driver/mysql"
)

const BEARER = "Bearer"

// Service interface exposed to clients
type AuthorizeService interface {
	// ValidateToken: bearer
	ValidateToken(string) error

	// CreateToken: name, password
	CreateToken(string, string) (Authorization, error)
}

////////////////////////
// Actual service
// This takes the following:
// - mysqlDb    DB for MySQL
// - redisPool  Redis pool
type authorizeService struct {
	mysqlDb   *sql.DB
	redisPool *pool.Pool
}

//////////
// METHODS on authorizationservice

////////////////
// ValidateToken
//
// what:
// Validates if the incoming authorization/bearer is OK.
// This will check the bearer string against what is in redis.
//
// params:
// bearer: Authorization bearer
//
// returns:
// error if bearer string is invalid
func (theService authorizeService) ValidateToken(bearer string) error {
	/////////////////
	// Redis
	conn, err := theService.redisPool.Get()
	if err != nil {
		fmt.Println("Got error when calling redisPool.Get: ", err)
		return ErrServerError
	}
	defer theService.redisPool.Put(conn)

	// Check the bearer string credentials

	// Key to query in redis
	redisHashName := "user:" + bearer

	// Check redis. If it is null, authentication failed
	_, err = conn.Cmd("HGET", redisHashName, "name").Str()

	if err != nil {
		// No authorization -> send a 401
		return ErrUnauthorized
	}

	// Authorization is OK, send a 200
	return nil
}

////////////////
// CreateToken
//
// what:
// Creates a new entry in redis and returns a token matching that entry.
//
// params:
// userName:     Name of user
// password: Password
func (theService authorizeService) CreateToken(userName string, password string) (Authorization, error) {
	/////////////////
	// Redis
	conn, err := theService.redisPool.Get()
	if err != nil {
		fmt.Println("Got error when calling redisPool.Get: ", err)
		return Authorization{}, ErrServerError
	}
	defer theService.redisPool.Put(conn)

	////////////////////
	// Mysql
	if err := theService.mysqlDb.Ping(); err != nil {
		theService.mysqlDb.Close()
		fmt.Println("Got error when calling mysql.Ping: ", err)
		return Authorization{}, ErrServerError
	}

	// Find user in database
	var user User
	// Can the DB info into 'user' variable
	err = theService.mysqlDb.QueryRow("SELECT user_id, name, user_group, password FROM user WHERE name = ?", userName).
		Scan(&user.UserId, &user.Name, &user.UserGroup, &user.Password)

	switch {
	case err == sql.ErrNoRows:
		fmt.Println("No matching user in db")
		return Authorization{}, ErrNotFound
	case err != nil:
		fmt.Println("Got error from select: ", err)
		return Authorization{}, ErrServerError
	default:
	}

	// Verify the password now
	passwordInDatabase := []byte(user.Password)
	passwordFromUser := []byte(password)

	isEncryptedError := bcrypt.CompareHashAndPassword(passwordInDatabase, passwordFromUser)
	if isEncryptedError != nil {
		// Passwords are NOT the same
		fmt.Println("Incoming password for '" + userName + "' is incorrect.")
		return Authorization{}, ErrUnauthorized
	}

	// User is ok. create uuid and token
	tokenUuid := uuid.Must(uuid.NewV4(), nil)
	token := tokenUuid.String()
	fullToken := BEARER + " " + token
	redisKey := "user:" + token

	// Put data into redis
	if conn.Cmd("HSET", redisKey, "name", userName).Err != nil {
		fmt.Println("Unable to set name '" + userName + "' in redis.")
		return Authorization{}, ErrServerError
	}
	if conn.Cmd("HSET", redisKey, "id", user.UserId).Err != nil {
		fmt.Println("Unable to set id '", user.UserId, "' in redis.")
		return Authorization{}, ErrServerError
	}
	if conn.Cmd("HSET", redisKey, "group", user.UserGroup).Err != nil {
		fmt.Println("Unable to set group in redis for '" + userName + "'.")
		return Authorization{}, ErrServerError
	}
	// expire data in a week
	if conn.Cmd("expire", redisKey, 60*60*24*7).Err != nil {
		fmt.Println("Unable to set expiration time for '" + userName + "' in redis.")
		return Authorization{}, ErrServerError
	}

	returnValue := Authorization{
		Token:     fullToken,
		UserId:    user.UserId,
		GroupName: user.UserGroup,
	}

	return returnValue, nil
}
