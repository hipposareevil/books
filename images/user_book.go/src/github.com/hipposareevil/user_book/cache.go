package main

// Cache layer

import (
	"fmt"
	"strconv"

	"time"

	"github.com/mediocregopher/radix.v2/pool"
	_ "github.com/mediocregopher/radix.v2/redis"
)

// Cache layer
type CacheLayer interface {
	// Set a key/value for a namespace
	Set(string, int, string)

	// Set a key/(byte)value for a namespace
	SetBytes(string, int, []byte)

	// Set multiple k/vs
	SetMultiple(string, map[int]string)

	// Get a key/value from the cache
	Get(string, int) string

	// Get a key/value from the cache
	GetBytes(string, int) []byte

	// Clear one key in the namespace
	Clear(string, int)

	// Clear all k/v from namespace
	ClearAll(string)
}

// actual service
type cacheLayer struct {
	redisPool *pool.Pool
}

////////////
// Set a value in the cache
//
// params:
// namespace Namespace for k/v
// key  Key to store
// value Value to store
func (theCache cacheLayer) Set(namespace string, key int, value string) {
	start := time.Now()

	conn, err := theCache.redisPool.Get()
	if err != nil {
		fmt.Println("Unable to get Redis for setting cache: ", err)
		return
	}
	defer theCache.redisPool.Put(conn)

	// convert key to string
	keyAsString := strconv.Itoa(key)

	// Set a value
	err = conn.Cmd("HSET", namespace, key, value).Err
	if err != nil {
		fmt.Println("Unable to set cache for "+namespace+"."+keyAsString+": ", err)
	}

	t := time.Now()
	elapsed := t.Sub(start)
	fmt.Println("cache.Set: ", elapsed)
}


////////////
// Set a value in the cache
//
// params:
// namespace Namespace for k/v
// key  Key to store
// value Byte Value to store
func (theCache cacheLayer) SetBytes(namespace string, key int, value []byte) {
    valueAsString := string(value[:])
    theCache.Set(namespace, key, valueAsString)
}

////////////
// Set a value in the cache
//
// params:
// namespace: Namespace for k/v
// kvMap: array of key values
func (theCache cacheLayer) SetMultiple(namespace string, kvMap map[int]string) {
	start := time.Now()

	conn, err := theCache.redisPool.Get()
	if err != nil {
		fmt.Println("Unable to get Redis for setting cache: ", err)
		return
	}
	defer theCache.redisPool.Put(conn)

	// Iterate over k/v map
	for key, value := range kvMap {
		// convert key to string
		keyAsString := strconv.Itoa(key)

		// Set a value
		err = conn.Cmd("HSET", namespace, key, value).Err
		if err != nil {
			fmt.Println("Unable to set cache for "+namespace+"."+keyAsString+": ", err)
		}
	}

	t := time.Now()
	elapsed := t.Sub(start)
	fmt.Println("cache.SetMultiple: ", elapsed)
}

////////////
// Clear a value in the cache
//
// params:
// namespace Namespace for k/v
// key  Key to clear
func (theCache cacheLayer) Clear(namespace string, key int) {
	conn, err := theCache.redisPool.Get()
	if err != nil {
		fmt.Println("Unable to get Redis for clearing cache: ", err)
		return
	}
	defer theCache.redisPool.Put(conn)

	// convert key to string
	keyAsString := strconv.Itoa(key)

	// clear
	err = conn.Cmd("HDEL", namespace, key).Err
	if err != nil {
		fmt.Println("Unable to delete cache for "+namespace+"."+keyAsString+": ", err)
	}
}

////////////
// Clear all values in the cache for a namespace
//
// params:
// namespace Namespace for k/v
func (theCache cacheLayer) ClearAll(namespace string) {
	conn, err := theCache.redisPool.Get()
	if err != nil {
		fmt.Println("Unable to get Redis for clearing cache: ", err)
		return
	}
	defer theCache.redisPool.Put(conn)

	// clear
	err = conn.Cmd("DEL", namespace).Err
	if err != nil {
		fmt.Println("Unable to delete cache for "+namespace+": ", err)
	}
}


////////////
// Get a byte array value from the cache
//
// params:
// namespace Namespace for k/v
// key  Key to retrieve
//
// returns:
// value, or empty byte array if none exists
func (theCache cacheLayer) GetBytes(namespace string, key int) []byte {
    stringValue := theCache.Get(namespace, key)
    var byteValue []byte

    if len(stringValue) > 0 {
        byteValue = []byte(stringValue)
    }

    return byteValue    
}


////////////
// Get a value from the cache
//
// params:
// namespace Namespace for k/v
// key  Key to retrieve
//
// returns:
// value, or "" if none exists
func (theCache cacheLayer) Get(namespace string, key int) string {
	conn, err := theCache.redisPool.Get()
	if err != nil {
		fmt.Println("Unable to get Redis for getting cache: ", err)
		return ""
	}
	defer theCache.redisPool.Put(conn)

	// convert key to string
	keyAsString := strconv.Itoa(key)

	// Set a value
	value, err := conn.Cmd("HGET", namespace, key).Str()
	if err != nil {
		fmt.Println("Unable to get cache for "+namespace+"."+keyAsString+": ", err)
		value = ""
	}

	return value
}
