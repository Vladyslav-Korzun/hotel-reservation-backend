# Hotel Reservation Application

Backend MVP for a hotel network reservation system. The project is built as a modular monolith with hexagonal architecture: domain use cases stay independent from Spring, REST, JPA, PostgreSQL, and generated OpenAPI DTOs.

## Architecture

Maven modules:

- `application/domain` - domain entities, value objects, repository ports, integration ports, facades, services, factories, predicates, domain exceptions
- `application/api-spec` - OpenAPI contract and generated REST API interfaces/DTOs
- `application/inbound-controller-rest` - REST controllers, DTO mappers, security, exception handling
- `application/outbound-repository-jpa` - JPA adapters, Spring Data repositories, ORM mapping, Liquibase migrations
- `application/springboot` - application bootstrap, bean wiring, transaction decorators, runtime configuration

Dependency direction:

```text
domain
api-spec
inbound-controller-rest -> domain + api-spec
outbound-repository-jpa -> domain
springboot -> inbound-controller-rest + outbound-repository-jpa
```

The domain module does not depend on Spring, JPA, REST, security, or OpenAPI-generated DTOs.

## Implemented MVP

Guest and public flows:

- list hotels
- view hotel details
- list hotel service offerings
- search available room types by city or hotel
- create reservation
- view reservation details
- list current user's reservations
- cancel reservation

Staff flows:

- list reservations
- check in reservation
- check out reservation
- mark reservation as no-show
- update room status

Admin flows:

- create/update hotels
- create/update room types
- create/update rooms
- create/update/deactivate hotel service offerings

Persistence and operations:

- PostgreSQL schema via Liquibase
- JPA adapters behind domain repository ports
- ORM XML mapping
- reservation price snapshot
- reservation service item snapshot
- guest persistence
- stay/occupancy records
- audit log for key actions
- JWT resource server security with `GUEST`, `STAFF`, `ADMIN`

Intentionally not included in MVP:

- loyalty system
- payments
- reports
- dynamic seasonal pricing
- Kafka/events
- microservices

## Main Rules

- `Reservation` protects reservation state transitions.
- `Room` protects room status transitions.
- `ServiceOffering` is a separate model with hotel ownership, price, active flag, and snapshot usage in reservations.
- Controllers only translate HTTP/DTO input and call facades.
- Services orchestrate use cases through domain ports.
- JPA and external systems are adapters, not business logic owners.

## REST API

The API contract is defined in:

```text
application/api-spec/src/main/resources/openapi/hotel-reservation.yaml
```

Implemented paths:

- `GET /hotels`
- `GET /hotels/{hotelId}`
- `GET /hotels/{hotelId}/services`
- `POST /rooms/search`
- `POST /reservations`
- `GET /reservations`
- `GET /reservations/{reservationId}`
- `POST /reservations/{reservationId}/cancel`
- `GET /me/reservations`
- `POST /staff/reservations/{reservationId}/check-in`
- `POST /staff/reservations/{reservationId}/check-out`
- `POST /staff/reservations/{reservationId}/no-show`
- `PATCH /staff/rooms/{roomId}/status`
- `POST /admin/hotels`
- `PUT /admin/hotels/{hotelId}`
- `POST /admin/room-types`
- `PUT /admin/room-types/{roomTypeId}`
- `POST /admin/rooms`
- `PUT /admin/rooms/{roomId}`
- `POST /admin/hotels/{hotelId}/services`
- `PUT /admin/hotels/{hotelId}/services/{serviceId}`
- `DELETE /admin/hotels/{hotelId}/services/{serviceId}`

## Local Run

Infrastructure:

```powershell
docker compose up -d
```

This starts:

- PostgreSQL on `localhost:5432`
- Keycloak on `localhost:8081`
- Keycloak realm import from `keycloak/realms/hotel-reservation-realm.json`

Application defaults are in:

```text
application/springboot/src/main/resources/application.yaml
```

If old local data conflicts with Liquibase migrations:

```powershell
docker compose down -v
docker compose up -d
```

## Testing

Run:

```powershell
mvn.cmd clean test
```

The test suite includes:

- domain unit tests
- service orchestration tests
- REST controller tests
- Spring Boot integration tests with Testcontainers/PostgreSQL
- JPA adapter tests
- security tests
- ArchUnit architecture rules
