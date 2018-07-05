package main


///////////////////
// Set of functions to make calls to other services


import (
	"fmt"
	"net/http"
	"strings"
	"time"
    "strconv"
	"io/ioutil"
	"encoding/json"
)

// Just need the author name
type author struct {
	Name string `json:"name"`
}


////////////
// Query the /author endpoint for author information
func getAuthorNameById(cache CacheLayer, bearer string, authorId int) string {
	start := time.Now()

	// Check cache
	authorName := cache.Get(AUTHOR_CACHE, authorId)
	if len(authorName) > 0 {
        fmt.Println("Got Author name from cache.")
		return authorName
	}
	url := "http://author:8080/author/" + strconv.Itoa(authorId)

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
	req.Header.Set("authorization", "Bearer "+bearer)

	// send request
	res, getErr := superClient.Do(req)
	if getErr != nil {
		fmt.Println("Unable to send request to /author")
		return ""
	}

	// Check status code
	if !strings.Contains(res.Status, "200") {
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
