# Hotel Reservation Backend Setup

Technical setup notes for the current project state.

## Current Scope

The current implementation is intentionally a single secured vertical slice for reservations:

- create reservation
- get reservation by id
- cancel reservation

The goal is to demonstrate one complete flow end-to-end:

- OpenAPI contract
- generated API interface and DTOs
- REST controller
- domain/application layer
- JPA persistence with XML ORM mapping
- PostgreSQL + Liquibase
- JWT security via Keycloak

## Modules

- `application/domain`
  Domain model, use cases, ports, shared exceptions and security abstractions.
- `application/api-spec`
  OpenAPI YAML and generated Spring API / DTO classes.
- `application/inbound-controller-rest`
  REST controllers, mappers, security layer, REST exception handling.
- `application/outbound-repository-jpa`
  JPA adapter, Spring Data repository, JPA entity, XML ORM mapping.
- `application/springboot`
  Main application, bean wiring, runtime configuration, Liquibase migrations.

## Implemented API

- `POST /reservations`
  Creates a reservation for the authenticated user.
- `GET /reservations/{reservationId}`
  Returns reservation details for the owner or an admin.
- `POST /reservations/{reservationId}/cancel`
  Cancels the reservation for the owner or an admin.

OpenAPI source:
- [application/api-spec/src/main/resources/openapi/hotel-reservation.yaml](./application/api-spec/src/main/resources/openapi/hotel-reservation.yaml)

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`

## Security

The backend is configured as a JWT resource server.

- JWT issuer: Keycloak realm `hotel-reservation`
- roles currently used:
  - `GUEST`
  - `ADMIN`
  - `STAFF` is prepared in Keycloak, but not yet used by reservation endpoints

Current reservation security rules:

- `POST /reservations` -> `GUEST` or `ADMIN`
- `GET /reservations/{reservationId}` -> `GUEST` or `ADMIN`
- `POST /reservations/{reservationId}/cancel` -> `GUEST` or `ADMIN`

Application-level ownership check:

- owner can access own reservation
- admin can access any reservation

## Domain Notes

`Reservation` is modeled as a domain entity, not just a transport record.

Current invariants inside the aggregate:

- `reservationId` must be present
- `hotelId` must be present
- `roomTypeId` must be present
- `checkOut` must be after `checkIn`
- `guestCount` must be greater than zero
- `cancelledAt` must be present only for `CANCELLED` reservations

Current domain behavior:

- create pending reservation
- ownership assertion
- cancellation

## Persistence

Database:
- PostgreSQL

Liquibase:
- creates the `reservations` table
- adds `cancelled_at`
- enforces status and cancellation consistency with DB constraints

JPA:
- persistence model is separated from domain model
- ORM mapping is defined in XML:
  [application/outbound-repository-jpa/src/main/resources/META-INF/orm.xml](./application/outbound-repository-jpa/src/main/resources/META-INF/orm.xml)

## Run

### 1. Start infrastructure

```powershell
docker compose up -d
```

This starts:

- PostgreSQL on `localhost:5432`
- Keycloak on `localhost:8081`

### 2. Start backend

From IntelliJ:

- run `HotelReservationApplication`

From terminal:

```powershell
mvn -pl application/springboot -am spring-boot:run
```

## PostgreSQL

JDBC URL:

```text
jdbc:postgresql://localhost:5432/hotel_reservation
```

Credentials:

- username: `hotel_user`
- password: `hotel_password`

## Keycloak

Realm:
- `hotel-reservation`

Client:
- `hotel-reservation-client`
- `Client authentication` = `On`
- `Direct access grants` = `On`

Realm roles:
- `GUEST`
- `STAFF`
- `ADMIN`

Example user:
- username: `guest1`
- password: `guest123`
- role: `GUEST`

## Postman Flow

### 1. Get token

```http
POST http://localhost:8081/realms/hotel-reservation/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded
```

Body:

```text
grant_type=password
client_id=hotel-reservation-client
client_secret=<client secret>
username=guest1
password=guest123
```

### 2. Create reservation

```http
POST http://localhost:8080/reservations
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "hotelId": 1,
  "roomTypeId": 2,
  "checkIn": "2026-05-10",
  "checkOut": "2026-05-12",
  "guestCount": 2
}
```

### 3. Get reservation

```http
GET http://localhost:8080/reservations/{reservationId}
Authorization: Bearer <access_token>
```

### 4. Cancel reservation

```http
POST http://localhost:8080/reservations/{reservationId}/cancel
Authorization: Bearer <access_token>
```

## Verification

Fast local verification:

```powershell
mvn -pl application/domain test
mvn -pl application/springboot -am -DskipTests compile
```

Full test suite:

```powershell
mvn test
```

Note:
- integration tests use Testcontainers
- Docker must be running for the full suite
