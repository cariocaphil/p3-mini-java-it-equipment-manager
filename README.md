# EquipIT

EquipIT is a Spring Boot API and React client for managing internal IT equipment loans.

## Prerequisites

- JDK 25
- Maven 3.9+
- Node.js LTS

## Run the API

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Run the client

```bash
cd frontend
npm install
npm run dev
```

## API

- `GET /api/equipment`
- `PUT /api/equipment/{id}/borrow`
- `PUT /api/equipment/{id}/return`

## Structure

The backend is organized around the equipment domain: entity and repository, service layer, DTO, and REST controller. The React client is kept separate and communicates with the API over HTTP.

## Business rules

- **Borrow** (`PUT /api/equipment/{id}/borrow`): only available equipment can be borrowed. The borrower's name and the borrow date are recorded.
- **Return** (`PUT /api/equipment/{id}/return`): only borrowed equipment can be returned. The borrower is cleared and `lastReturnedAt` is set to the return date. Borrowing again does not change `lastReturnedAt`.
- Errors are returned as RFC 9457 `ProblemDetail` responses: `404` if the equipment does not exist, `409` if the operation is invalid for its current state (already borrowed / not borrowed).
- Concurrent requests on the same equipment are not protected against (out of scope for now).

## Tests

```bash
mvn test
```
