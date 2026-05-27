FROM golang:1.19.2-bullseye
 
WORKDIR /app
 
COPY server.go .
COPY go.mod .
 
# RUN go mod download
 
RUN go build -o http-logger
 
EXPOSE 8887
 
CMD [ "./http-logger" ]