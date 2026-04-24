# Smart Campus API
## Overview
This is my Smart Campus API built for the 5COSC022W Client-Server Architectures coursework. The API is built using JAX-RS with Jersey and runs on an embedded Grizzly HTTP server so there is no need to install a separate server like Tomcat. It manages campus Rooms and Sensors and keeps a historical log of Sensor Readings. I used in-memory data structures like ConcurrentHashMap to store all the data as no database is allowed.

Base URL: http://localhost:8080/api/v1

Tech used: Java, JAX-RS (Jersey 3.1.3), Grizzly HTTP Server, Maven

How to Build and Run
What you need

Java JDK 11 or higher
Maven installed
Any Java IDE (I used NetBeans)

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



## Sample curl Commands

### 1. Get API Discovery
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CAV-101","name":"Cavendish Lecture Hall","capacity":120}'
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4. Create a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CAV-TEMP-01","type":"Temperature","status":"ACTIVE","currentValue":21.3,"roomId":"CAV-101"}'
```

### 5. Get Sensors filtered by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 6. Add a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CAV-TEMP-01/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.4}'
```

### 7. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/CAV-TEMP-01/readings
```

### 8. Try deleting a Room with Sensors (409 error)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/CAV-101
```


## Report — Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle
By default JAX-RS creates a new instance of a resource class for every request that comes in. This means classes like RoomResource and SensorResource are not singletons. Because of this I cannot store data inside the resource classes themselves as it would be lost after each request. To fix this I created a DataStore class which is a singleton that holds all the data in ConcurrentHashMaps. All the resource classes call DataStore.getInstance() to access the same shared data. I used ConcurrentHashMap instead of a regular HashMap because multiple requests can come in at the same time and a regular HashMap is not thread safe which could cause data corruption.

### Part 1.2 HATEOAS
HATEOAS (Hypermedia as the Engine of Application State) means the API response includes links that tell the client where to find other resources. In my DiscoverResource class the GET /api/v1 endpoint returns links to /api/v1/rooms and /api/v1/sensors. This means the client does not need to hardcode URLs or rely on external documentation to know where things are. If the URL structure ever changes the client can just follow the links in the response and it will still work. This is much better than static documentation which goes out of date every time the API changes.

### Part 2.1 IDs vs Full Objects
My GET /api/v1/rooms endpoint returns full room objects including the id, name, capacity and sensorIds. Returning only IDs would make the response smaller and save bandwidth but the client would then need to make a separate request for every room to get the details which means a lot more round trips. Returning full objects gives the client everything it needs in one request. For a campus management system where someone needs to see all room details at once this is the better approach.

### Part 2.2 DELETE Idempotency
The DELETE operation is partially idempotent in this implementation. Idempotency means that making the same request multiple times produces the same result as making it once. In this API, the first DELETE request for a room that exists and has no sensors will successfully remove it and return 204 No Content. Any subsequent DELETE request for the same room ID will return 404 Not Found because the room no longer exists. While the server state is the same after each call (the room remains absent), the HTTP response code changes between calls. This is accepted behaviour in REST — the important thing is that the resource ends up in the same state regardless of how many times the request is sent, and no unintended side effects occur from repeated calls.

### Part 3.1  @Consumes Mismatch
The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the endpoint only accepts requests with a Content-Type of application/json. If a client sends data in a different format such as text/plain or application/xml, JAX-RS will automatically reject the request before it even reaches the resource method. The framework returns an HTTP 415 Unsupported Media Type response, indicating that the server cannot process the format of the request body. This protects the API from malformed or unexpected input without requiring any manual checking inside the method.

### Part 3.2  @QueryParam vs Path-Based Filtering
I used @QueryParam("type") in SensorResource so clients can filter sensors like GET /api/v1/sensors?type=Temperature. This is better than putting the filter in the path like /api/v1/sensors/type/Temperature because query parameters are optional by design. If no type is provided the full list is returned, if one is provided it filters the results. Putting filters in the path would also clash with the existing /{sensorId} path and make the API harder to extend if more filters are needed in the future.~

### Part 4.1 Sub-Resource Locator Pattern
The Sub-Resource Locator pattern improves API maintainability by delegating responsibility for nested resources to dedicated classes. Instead of defining every possible path combination in one large resource class, each class has a single focused responsibility. In this API, SensorResource handles sensor-level operations and delegates reading-related operations to SensorReadingResource. This separation makes the codebase easier to read, test, and modify. In large APIs with many levels of nesting, putting everything in one controller class would result in a bloated, hard-to-maintain file. The locator pattern mirrors good object-oriented design principles by keeping classes small and focused.

### Part 5.2 — 422 vs 404
When a client posts a sensor with a roomId that doesn't exist the URL /api/v1/sensors is valid and the JSON is correctly formatted. The problem is that the roomId value inside the body references something that doesn't exist. A 404 would suggest the URL itself doesn't exist which is wrong and confusing. A 422 Unprocessable Entity is more accurate because it tells the client the server understood the request but couldn't process it because of bad data inside the payload. My LinkedResourceNotFoundExceptionMapper handles this and returns the 422 response.

### Part 5.4 — Stack Trace Security Risks
If the API returned raw Java stack traces it would expose a lot of sensitive information. Attackers could see which libraries and versions the app uses and look up known vulnerabilities for those. They could also see the internal class names and package structure which helps them understand how the app works and find weak points to exploit. My GlobalExceptionMapper catches every unhandled error and returns a simple generic 500 message so none of that internal information ever reaches the client.

### Part 5.5 — Filters vs Manual Logging
Using JAX-RS filters for cross-cutting concerns like logging is far superior to manually inserting Logger.info() statements in every resource method. Filters implement the DRY (Don't Repeat Yourself) principle the logging logic is written once and automatically applied to every request and response without touching individual resource classes. Manual logging requires every developer working on the codebase to remember to add log statements, making it error-prone and inconsistent. Filters also make it easy to modify logging behaviour in one place for example, adding request IDs or timestamps without having to update dozens of methods across the codebase. This separation of concerns keeps resource classes focused on business logic rather than infrastructure concerns.
