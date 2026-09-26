# Filmoteka User

User profiles and personal film lists for Filmoteka.

Profiles and lists are stored in PostgreSQL. Lists contain film IDs; film metadata is managed by the catalog service.

## Tech stack

- Java 25
- Spring Boot 4
- Spring Web, Validation and Actuator
- Spring Data JPA
- Spring Security with Keycloak JWT authentication
- Spring Kafka
- PostgreSQL and Flyway
- MapStruct
- OpenAPI / Swagger
- JUnit, Mockito and Testcontainers

## Requirements

- JDK 25
- PostgreSQL
- Kafka
- Keycloak
- Catalog service for film validation
- Docker, if you want to run the full test suite

## Run locally

PostgreSQL, Keycloak and Kafka can be started using the [shared Docker Compose setup](https://github.com/mks-filmoteka/filmoteka).

If you are not using the database from that setup, create it manually:

```sql
CREATE DATABASE filmoteka_user;
```

The `filmoteka` schema and tables are created by Flyway migrations on startup.

Set the environment variables. These examples match the default local Docker Compose settings; adjust credentials if you changed them:

```powershell
$env:DATASOURCE_URL = "jdbc:postgresql://localhost:5433/filmoteka_user"
$env:DATASOURCE_USERNAME = "filmoteka_user"
$env:DATASOURCE_PASSWORD = "filmoteka_user"
$env:CATALOG_API_URL = "http://localhost:8080/api/v1"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
$env:AUTH_ISSUER_URI = "http://localhost:8180/realms/filmoteka"
$env:AUTH_JWK_SET_URI = "http://localhost:8180/realms/filmoteka/protocol/openid-connect/certs"
$env:AUTH_AUDIENCE = "filmoteka-api"
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
$env:KAFKA_FILM_DELETED_TOPIC = "filmoteka.film-deleted.v1"
$env:KAFKA_USER_CONSUMER_GROUP_ID = "filmoteka-user"
$env:KAFKA_AUTO_OFFSET_RESET = "earliest"
```

For bash:

```bash
export DATASOURCE_URL="jdbc:postgresql://localhost:5433/filmoteka_user"
export DATASOURCE_USERNAME="filmoteka_user"
export DATASOURCE_PASSWORD="filmoteka_user"
export CATALOG_API_URL="http://localhost:8080/api/v1"
export CORS_ALLOWED_ORIGINS="http://localhost:5173"
export AUTH_ISSUER_URI="http://localhost:8180/realms/filmoteka"
export AUTH_JWK_SET_URI="http://localhost:8180/realms/filmoteka/protocol/openid-connect/certs"
export AUTH_AUDIENCE="filmoteka-api"
export KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
export KAFKA_FILM_DELETED_TOPIC="filmoteka.film-deleted.v1"
export KAFKA_USER_CONSUMER_GROUP_ID="filmoteka-user"
export KAFKA_AUTO_OFFSET_RESET="earliest"
```

For IDE runs, set these variables in the run configuration.

Start the app:

```bash
./mvnw spring-boot:run
```

On Windows, replace `./mvnw` with `.\mvnw.cmd` for this and the commands below.

The app runs at [http://localhost:8082](http://localhost:8082).

Check readiness at [http://localhost:8082/actuator/health/readiness](http://localhost:8082/actuator/health/readiness).
Stop the app with `Ctrl+C`, or the IDE's Stop button.

## API docs

- [Swagger UI](http://localhost:8082/swagger-ui.html)
- [OpenAPI JSON](http://localhost:8082/api-docs)

## Main endpoints

```text
GET    /api/v1/profile
PUT    /api/v1/profile

GET    /api/v1/film-lists
GET    /api/v1/film-lists/{id}
POST   /api/v1/film-lists
PUT    /api/v1/film-lists/{id}
DELETE /api/v1/film-lists/{id}

PUT    /api/v1/film-lists/{id}/films/{filmId}
PATCH  /api/v1/film-lists/{id}/films
DELETE /api/v1/film-lists/{id}/films/{filmId}
```

These endpoints require a Keycloak access token, sent as `Authorization: Bearer <token>`. Users can access only their own profile and lists.

Profiles are created automatically on the first profile or list request. `PUT /api/v1/profile` updates the display name.

The list PATCH endpoint accepts `addedFilmIds` and `removedFilmIds`. New film IDs are checked against catalog before being added.

## Film cleanup

Film-deletion events from Kafka remove the deleted film from all lists asynchronously. Lists and their other films are kept, and repeated events are safe to process.

Failed cleanup uses retry topics and then a `.user.dlt` topic. Dead-letter records are retained for 30 days and require manual investigation; automatic DLT processing is disabled.

A periodic reconciliation also checks stored film IDs against catalog and removes missing films. It is enabled by default and can be disabled with `app.reconciliation.enabled=false`.

## Build

Build the JAR and run the tests with Docker running:

```bash
./mvnw clean verify
```

The JAR is written to `target/`. Remove generated build files with `./mvnw clean`.

## Tests

```bash
./mvnw test
```

Database tests use Testcontainers, so Docker should be running. Kafka integration tests start an embedded broker.

## Notes

- With the example settings, PostgreSQL runs on `localhost:5433` and CORS allows `http://localhost:5173`.
- Health details, `/actuator/info` and `/actuator/metrics` require the `ADMIN` role.