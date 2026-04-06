# Hotel Reservation Backend Setup

Modular monolith starter for a hotel reservation backend based on hexagonal architecture.

## Baseline

- Java 25
- Maven multi-module project
- Spring Boot runtime
- PostgreSQL + Liquibase
- OpenAPI-first workflow

## Architecture

The target architecture is documented in [ARCHITECTURE.md](./ARCHITECTURE.md).

Reference project for structural inspiration:
`C:\Users\Vlad\Desktop\start\Пример\FSA-hexagonal-architecture-master`

We use that project as a module and layering reference, not as a source of domain decisions.

Domain diagram from earlier analysis is also kept in the repository:
- [hotel_reservation_diagram.png](./hotel_reservation_diagram.png)

## Modules

- `application/domain` - domain model, use cases, ports
- `application/api-spec` - OpenAPI contract and generated API/DTO classes
- `application/inbound-controller-rest` - REST inbound adapter
- `application/outbound-repository-jpa` - JPA outbound adapter
- `application/springboot` - runtime bootstrapping and configuration

## First implementation target

The current first submission covers a secured end-to-end reservation flow:

- OpenAPI contract
- generated interface
- REST controller
- domain facade
- JPA persistence
- JWT security
- PostgreSQL + Liquibase migration

## Implemented API

- `POST /reservations` - create reservation
- `GET /reservations/{reservationId}` - get reservation by id
- `DELETE /reservations/{reservationId}` - delete reservation by id

OpenAPI source:
- [application/api-spec/src/main/resources/openapi/hotel-reservation.yaml](./application/api-spec/src/main/resources/openapi/hotel-reservation.yaml)

Swagger UI after startup:
- `http://localhost:8080/swagger-ui/index.html`

## Run

### 1. Start infrastructure

```powershell
docker compose up -d
```

This starts:
- PostgreSQL on `localhost:5432`
- Keycloak on `localhost:8081`

### 2. Start the backend

From IntelliJ:
- run `HotelReservationApplication`
- set `Active profiles` to `keycloak`

From terminal:

```powershell
mvn -pl application/springboot -am spring-boot:run -Dspring-boot.run.profiles=keycloak
```

The backend uses PostgreSQL by default.

## Keycloak Setup

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

### 4. Delete reservation

```http
DELETE http://localhost:8080/reservations/{reservationId}
Authorization: Bearer <access_token>
```

## Database

PostgreSQL connection:

```text
jdbc:postgresql://localhost:5432/hotel_reservation
```

Credentials:
- username: `hotel_user`
- password: `hotel_password`

Liquibase changelog:
- [application/springboot/src/main/resources/db/changelog/db.changelog-master.yaml](./application/springboot/src/main/resources/db/changelog/db.changelog-master.yaml)

## Verification

Run tests:

```powershell
mvn test
```
