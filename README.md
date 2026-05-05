# Hotel Reservation Application

Backend application for hotel room availability search, reservation management, and staff stay operations.

## Project Scope

The current implementation covers these backend capabilities:

- search available room types by city or by specific hotel
- create a reservation for an authenticated guest or administrator
- view reservation details
- cancel a reservation
- list all reservations for staff and administrators
- perform guest check-in
- perform guest check-out

The codebase is organized as a modular Spring Boot application with these layers:

- `application/domain` for `domain.*`, `service.*` facades, services, ports, and exceptions
- `application/api-spec` for OpenAPI-generated contracts
- `application/inbound-controller-rest` for REST controllers and security
- `application/outbound-repository-jpa` for persistence adapters
- `application/springboot` for application wiring, Liquibase, and integration tests

## Main Concepts

### Room Status

- `AVAILABLE`
- `OCCUPIED`
- `CLEANING`
- `MAINTENANCE`
- `OUT_OF_SERVICE`

### Reservation Status

- `PENDING`
- `CONFIRMED`
- `CHECKED_IN`
- `CHECKED_OUT`
- `CANCELLED`
- `NO_SHOW`

## Implemented Use Cases

### UC01 Create Reservation

Authenticated guest selects hotel, stay dates, room type, and guest count. The system validates hotel and room type, checks room inventory for the selected period, and creates a reservation in status `PENDING`.

### UC02 Check-In

Staff or administrator checks in a reservation on the allowed date. The system locks the reservation, selects an available room of the requested type, assigns the room to the reservation, and changes:

- reservation status to `CHECKED_IN`
- room status to `OCCUPIED`

### UC03 Check-Out

Staff or administrator checks out a reservation that is currently checked in. The system locks the reservation, completes the stay, and changes:

- reservation status to `CHECKED_OUT`
- room status to `CLEANING`

## Business Rules

- `checkOut` must be after `checkIn`
- `guestCount` must be greater than zero
- reservation can only be created for an active hotel
- reservation can only be created for a room type that can host the requested guest count
- active overlapping reservations consume room inventory
- cancelled, checked-in, checked-out, and no-show reservations cannot be cancelled arbitrarily
- checked-in and checked-out reservations must have an assigned room
- only staff or administrators can perform stay operations
- guests can access only their own reservations
- staff and administrators can access all reservations

## Local Run

Infrastructure for local development is defined in [docker-compose.yml](docker-compose.yml):

- PostgreSQL on `localhost:5432`
- Keycloak on `localhost:8081`
- realm import from [keycloak/realms/hotel-reservation-realm.json](./keycloak/realms/hotel-reservation-realm.json)
- demo hotel catalog seeded automatically on an empty database

Application defaults are in `application/springboot/src/main/resources/application.yaml`.

If local startup fails during Liquibase because of old dev data and foreign keys, reset the
local PostgreSQL volume and start again:

```powershell
docker compose down -v
docker compose up -d
```

If your Keycloak realm was created manually before these changes, recreate Keycloak once so the
project-managed realm import is applied:

```powershell
docker compose down -v
docker compose up -d
```

## Testing

- domain and module tests run with Maven
- integration coverage is provided by `CreateReservationFlowIntegrationTest`
- integration test execution requires Docker because it uses Testcontainers with PostgreSQL
