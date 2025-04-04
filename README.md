# API do Zero

A simple and lightweight REST API built with vanilla Java (no frameworks).

## Overview

This project demonstrates how to build a REST API from scratch using only standard Java libraries, without relying on external frameworks like Spring. It implements a complete CRUD for user management with:

- HTTP server using `com.sun.net.httpserver`
- Multiple persistence options (SQLite and text file)
- JSON request/response handling
- REST architectural style

## Features

- **User Management**: Create, read, update, and delete operations for users
- **RESTful Endpoints**: Standard HTTP methods with proper status codes
- **Multiple Storage Options**: 
  - SQLite database persistence (default)
  - Text file-based persistence (alternative)
- **Email Validation**: Basic validation to prevent duplicate emails
- **CORS Support**: Cross-origin resource sharing headers

## API Endpoints

| Method | Endpoint      | Description             |
|--------|---------------|-------------------------|
| GET    | `/api/user`   | Get all users           |
| GET    | `/api/user/1` | Get user by ID          |
| POST   | `/api/user`   | Create a new user       |
| PUT    | `/api/user/1` | Update an existing user |
| DELETE | `/api/user/1` | Delete a user           |

## Request and Response Examples

### Creating a user

```
POST /api/user
Content-Type: application/json

{
"name": "John Doe",
"email": "john@example.com"
}
```

Response:
```
HTTP/1.1 201 Created
Content-Type: application/json

{
"id": 1,
"name": "John Doe",
"email": "john@example.com"
}
```

## Project Structure

- `Main.java` - Application entry point and server configuration
- `controllers/` - HTTP request handlers
- `dao/` - Data access objects for persistence
  - `impl/UserDAOSqliteImpl.java` - SQLite implementation
  - `impl/UserDAOTextFileImpl.java` - Text file implementation
- `entities/` - Domain model classes

## Running the Application

### Prerequisites
- Java 21
- Maven

### Build and Run

This project uses Maven Shade Plugin to create a fat JAR with all dependencies included:

```bash
mvn clean package
java -jar target/apidozero-1.0-SNAPSHOT.jar
```

The server will start on port 3000 by default.

### Switching Persistence Implementations

To change between SQLite and text file storage, modify the `Main.java` file:

```java
// For SQLite implementation (default)
private final static UserDAO userDAO = new UserDAOSqliteImpl();

// For text file implementation
// private final static UserDAO userDAO = new UserDAOTextFileImpl();
```

## Educational Purpose

This project was created for educational purposes to understand how modern web frameworks function under the hood, focusing on:

1. HTTP protocol fundamentals
2. Request and response handling
3. REST architectural principles
4. Data persistence patterns (both SQL and file-based)
5. Java I/O operations
6. DAO pattern with multiple implementations

## License

MIT