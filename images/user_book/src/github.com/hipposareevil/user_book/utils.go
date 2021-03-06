package main

import (
	"strconv"
	"strings"
)

////////
// Return a untion of the two incoming arrays
//
//
func Union(left []int, right []int) []int {
	var result []int

	for _, leftValue := range left {
		// See if leftValue is in right array
		if inArray(leftValue, right) {
			result = append(result, leftValue)
		}
	}

	return result
}

func inArray(valueToCheck int, array []int) bool {
	for _, currentValue := range array {
		if currentValue == valueToCheck {
			return true
		}
	}
	return false
}

////////////
// Split a CSV string into array
func splitCsvStringToArray(csv string) []string {
	if len(csv) == 0 {
		return []string{}
	} else {
		array := strings.Split(csv, ",")
		return array
	}
}

//////////
// Convert string array to CSV string
func convertArrayToCsv(array []string) string {
	return strings.Join(array, ",")
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

////////////
// Convert Tags structure into map of Tag objects indexed by name
//
func convertTagsJsonToArray(tags Tags) map[string]Tag {
	newMap := make(map[string]Tag)

	for _, item := range tags.Data {
		name := item.Name
		newMap[name] = item
	}

	return newMap
}
