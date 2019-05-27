package main

import (
	"strconv"
	"strings"
)

////////////
// Split a CSV string into array
func splitCsvStringToArray(subjectCsv string) []string {
	if len(subjectCsv) > 0 {
		return strings.Split(subjectCsv, ",")
	} else {
		return make([]string, 0)
	}
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
