# Smart Campus API

## Overview
The Smart Campus API is a RESTful web service built using JAX-RS (Jersey) and an embedded Grizzly HTTP server. It provides endpoints to manage university campus Rooms and Sensors, and maintain a historical log of Sensor Readings. The API follows REST architectural principles including proper HTTP status codes, JSON responses, and a logical resource hierarchy.

**Base URL:** `http://localhost:8080/api/v1`

**Technology Stack:** Java, JAX-RS (Jersey 3.1.3), Grizzly HTTP Server, Maven

---

## How to Build and Run

### Prerequisites
- Java JDK 11 or higher
- Maven installed
- NetBeans IDE (or any Java IDE)

### Steps
1. Clone the repository:
```bash
   git clone https://github.com/Marcfernz/SmartCampusAPI.git
```
2. Navigate to the project folder:
```bash
   cd SmartCampusAPI
```
3. Build the project:
```bash
   mvn clean install
```
4. Run the server:
```bash
   mvn exec:java
```
5. The API will be available at `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Get API Discovery
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Create a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```

### 5. Get Sensors filtered by type
```bash
curl -X GET http://localhost:8080/api/v1/sensors?type=Temperature
```

### 6. Add a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

### 7. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 8. Try deleting a Room with Sensors (409 error)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

---

## Report — Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle
By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request. This is known as per-request scope. The runtime does not treat resource classes as singletons. This architectural decision has a significant impact on how in-memory data is managed. Since each request gets its own resource instance, any data stored as an instance field would be lost after the request completes. To prevent data loss, a shared singleton such as the DataStore class must be used to hold all in-memory data structures. Additionally, since multiple requests can arrive simultaneously, thread-safe structures like ConcurrentHashMap must be used instead of a regular HashMap to prevent race conditions where two threads modify the same data at the same time.

### Part 1.2 — HATEOAS
HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links that guide the client to related resources and available actions. This is considered a hallmark of advanced REST design because it makes the API self-describing — clients do not need to hardcode URLs or rely entirely on external documentation to navigate the system. For client developers, this is beneficial because if the API changes its URL structure, clients that follow the links in responses will automatically adapt, whereas clients relying on static documentation would break. It also reduces the learning curve for new developers consuming the API, as the responses themselves advertise what actions are available next.

### Part 2.1 — IDs vs Full Objects
Returning only IDs is more efficient in terms of network bandwidth because less data is transmitted per response. However, it forces the client to make additional requests to fetch the details of each room, increasing the number of round trips and adding complexity to the client-side code. Returning full room objects increases the response payload size but gives the client everything it needs in a single request, reducing latency and simplifying client-side processing. For a campus management system where rooms may have many fields, returning full objects is generally the better approach as it prioritises usability and reduces the number of API calls required.

### Part 2.2 — DELETE Idempotency
The DELETE operation is partially idempotent in this implementation. Idempotency means that making the same request multiple times produces the same result as making it once. In this API, the first DELETE request for a room that exists and has no sensors will successfully remove it and return 204 No Content. Any subsequent DELETE request for the same room ID will return 404 Not Found because the room no longer exists. While the server state is the same after each call (the room remains absent), the HTTP response code changes between calls. This is accepted behaviour in REST — the important thing is that the resource ends up in the same state regardless of how many times the request is sent, and no unintended side effects occur from repeated calls.

### Part 3.1 — @Consumes Mismatch
The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the endpoint only accepts requests with a Content-Type of application/json. If a client sends data in a different format such as text/plain or application/xml, JAX-RS will automatically reject the request before it even reaches the resource method. The framework returns an HTTP 415 Unsupported Media Type response, indicating that the server cannot process the format of the request body. This protects the API from malformed or unexpected input without requiring any manual checking inside the method.

### Part 3.2 — @QueryParam vs Path-Based Filtering
Using @QueryParam for filtering (e.g., /sensors?type=CO2) is considered superior to embedding the filter in the URL path (e.g., /sensors/type/CO2) for several reasons. Query parameters are semantically designed for filtering, searching, and pagination — they are optional by nature, meaning the same endpoint handles both filtered and unfiltered requests cleanly. Path parameters are designed to identify a specific resource, not to describe a search criterion. Embedding filter logic in the path also pollutes the URL structure, makes the API harder to extend, and can conflict with other path-based routes. Query parameters are also more familiar to client developers and are better supported by API tooling and documentation standards.

### Part 4.1 — Sub-Resource Locator Pattern
The Sub-Resource Locator pattern improves API maintainability by delegating responsibility for nested resources to dedicated classes. Instead of defining every possible path combination in one large resource class, each class has a single focused responsibility. In this API, SensorResource handles sensor-level operations and delegates reading-related operations to SensorReadingResource. This separation makes the codebase easier to read, test, and modify. In large APIs with many levels of nesting, putting everything in one controller class would result in a bloated, hard-to-maintain file. The locator pattern mirrors good object-oriented design principles by keeping classes small and focused.

### Part 5.2 — 422 vs 404
HTTP 404 Not Found implies that the requested URL or resource does not exist. However, when a client POSTs a valid JSON payload containing a roomId that does not exist in the system, the URL itself is valid and the request is syntactically correct. The problem is a semantic validation failure — the referenced resource inside the payload cannot be found. HTTP 422 Unprocessable Entity is more accurate in this case because it signals that the server understood the request and its format, but could not process it due to a logical or referential error in the data. Using 404 here would be misleading to client developers debugging the issue.

### Part 5.4 — Stack Trace Security Risks
Exposing internal Java stack traces to external API consumers is a significant security risk. A stack trace reveals the internal structure of the application, including class names, method names, file names, and line numbers. An attacker can use this information to identify which frameworks and libraries are being used, look up known vulnerabilities for those specific versions, understand the application's internal logic and identify weak points, and craft targeted attacks such as injection or path traversal based on exposed class structures. By returning a generic 500 Internal Server Error message instead, the API prevents information leakage while still communicating that something went wrong.

### Part 5.5 — Filters vs Manual Logging
Using JAX-RS filters for cross-cutting concerns like logging is far superior to manually inserting Logger.info() statements in every resource method. Filters implement the DRY (Don't Repeat Yourself) principle — the logging logic is written once and automatically applied to every request and response without touching individual resource classes. Manual logging requires every developer working on the codebase to remember to add log statements, making it error-prone and inconsistent. Filters also make it easy to modify logging behaviour in one place — for example, adding request IDs or timestamps — without having to update dozens of methods across the codebase. This separation of concerns keeps resource classes focused on business logic rather than infrastructure concerns.
