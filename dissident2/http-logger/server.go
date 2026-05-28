package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
)

// Define a struct to parse the incoming JSON data
type MessageStruct struct {
	SrcEntity string
    TrgEntity string
    Timestamp int64
    Content   interface{} 
    Group  string 
    SubGroup  string 
}

var (
	// UI Backend Endpoint constant
	uiBackendEndpoint = "http://host.docker.internal:48024/events"
)

func httpLoggerHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Only POST method is allowed", http.StatusMethodNotAllowed)
		return
	}

	log.Println("go server received event, data is:")
	
	// Decode the JSON body
	var data MessageStruct
	err := json.NewDecoder(r.Body).Decode(&data)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}
	
	log.Println(data)

	// Send data to the UI backend
	sendToUIBackend(data)

	fmt.Fprintf(w, "Data processed and sent to UI Backend")
}

func sendToUIBackend(data MessageStruct) {
	// Marshal data to JSON
	jsonData, err := json.Marshal(data)
	if err != nil {
		log.Printf("Error marshaling data: %v", err)
		return
	}

	// Create a buffer from the JSON byte slice
	buffer := bytes.NewBuffer(jsonData)

	// Send POST request to UI backend
	resp, err := http.Post(uiBackendEndpoint, "application/json", buffer)
	if err != nil {
		log.Printf("Error sending request to UI backend: %v", err)
		return
	}
	defer resp.Body.Close()

	// Log the response from the UI backend
	log.Printf("Sent to UI Backend, response status: %s", resp.Status)
}

func main() {
	http.HandleFunc("/http-logger", httpLoggerHandler)
	log.Println("Starting server on :8887")
	log.Fatal(http.ListenAndServe(":8887", nil))
}

