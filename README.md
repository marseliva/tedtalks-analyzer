# TedTalks Analyzer

A Java Spring Boot application for analyzing TED Talks data.  
Provides CRUD operations, asynchronous CSV uploads, and data analytics, including detection of suspicious data such as unusual like patterns.

---

## Project Structure

```plaintext
tedtalks-analyzer/
├── src/
    ├── main/
        ├── java/      # Java source code
        └── resources/ # Resource files
    └── test/
        ├── java/      # Test source code
        └── resources/
├── build.gradle      # Gradle build configuration
├── gradle.properties # Gradle properties file
├── Dockerfile        # Docker build file
├── docker-compose.yml # Docker Compose file
├── settings.gradle   # Gradle settings file
├── README.md         # Project overview

```

## Installation & Setup

### Using Gradle

```bash
# Clone the repository
git clone https://github.com/marseliva/tedtalks-analyzer

# Navigate to the project directory
cd tedtalks-analyzer

# Clean previous builds and install dependencies
./gradlew clean build

# Alternatively, if you prefer
./gradlew clean install

# Start services without logs
docker-compose up -d

# Stop services
docker-compose down
```

## Components Used

- **Java 21** — Base runtime environment (OpenJDK 21-slim)
- **PostgreSQL 15** — Relational database for persistent storage
- **Redis** — In-memory data store for caching
- **Spring Boot (assumed)** — Framework for building Java applications

## Core Features

- **CRUD operations on TedTalks**: Full Create, Read, Update, Delete operations on a single entity `TedTalks`.
- **Swagger Documentation**: Explore API via [Swagger UI](http://localhost:8080/swagger-ui/index.html).
- **Asynchronous CSV Upload**: Import TedTalks by uploading a CSV file via `/api/import`, processed asynchronously.
- **Event-Driven Processing**: Upload triggers a `TedTalkImportEvent`, processed using a background thread pool for scalability.
- **Automatic Analytics**: After CSV import, analytics on the imported talks are calculated and stored.
- **Data Handling Flow**:
    - When a user uploads a CSV file, an asynchronous event-driven process starts.
    - Each record from the file is validated and saved in batches into PostgreSQL.
    - Saved records are also placed into Redis cache for faster access.
    - Import statistics are logged immediately after processing.
    - Historical analytics can be retrieved anytime using the appropriate API endpoints.
    - Cache does not auto-evict; instead, a scheduled job runs daily at midnight to verify and refresh data consistency.


## API Endpoints

- `POST /api/import` — Upload a CSV file (multipart form-data). Triggers async import process.
- `GET /api/v1/tedtalks` — List all TedTalks.
- `GET /api/v1/tedtalks/{id}` — Get a specific TedTalk by ID.
- `POST /api/v1/tedtalks` — Create a new TedTalk.
- `PUT /api/v1/tedtalks/{id}` — Update an existing TedTalk.
- `DELETE /api/v1/tedtalks/{id}` — Delete a TedTalk.
- `POST /api/analytics/search` — Search TedTalks based on various parameters.
- `GET /api/analytics/top-speakers` — Returns top speakers of all the time.
- `GET /api/analytics/talks-count/{year}` — Returns talks count based on year.
- `GET /api/analytics/best-talk/{year}` — Returns most popular speaker based on year.

## Future Improvements

- [ ] Add more unit and integration tests to improve code quality and stability.
- [ ] Improve exception handling
- [ ] Currently, the statistics for the processed file are written to the logs.
  In the future, this could be improved by saving per-file statistics to a database or using SSE if a UI is available.
- [ ] Increase the number and depth of collected analytics (e.g., analyze viewer engagement rates, TED talk category trends).
- [ ] Potentially revise cache eviction policy to ensure data accuracy over time.
- [ ] Move file ingestion to external storage (e.g., S3) with event-driven parsing for scalability.
- [ ] Add authentication and authorization.
- [ ] Implement pagination and sorting for listing APIs.
- [ ] Improve validation and error handling during CSV import.
- [ ] Use database migration tool instead of auto schema generation.
- [ ] Change metrics calculation to something more representative.
