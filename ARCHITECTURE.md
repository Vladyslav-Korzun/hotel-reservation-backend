# Hotel Reservation Architecture

## Current Architectural Goal

The project is built as a modular monolith with hexagonal architecture and an API-first workflow.

For the current project state, the implemented scope now includes:

- reservation creation, retrieval, cancellation, and listing
- room availability search
- staff stay operations: check-in and check-out
- full flow from HTTP request to PostgreSQL persistence

## Architectural Formula

`Modular Monolith + Hexagonal Architecture + API-first`

In this project that means:

- domain logic is isolated from Spring, JPA and REST
- OpenAPI defines the contract first
- REST is an inbound adapter
- JPA/PostgreSQL is an outbound adapter
- Spring Boot is used only for wiring and runtime

## Module Structure

All runtime modules live under `application/`.

- `application/domain`
  Domain module that contains both `com.hotel.management.domain.*` and `com.hotel.management.service.*`.
- `application/api-spec`
  OpenAPI contract and generated interfaces / DTOs.
- `application/inbound-controller-rest`
  REST adapter, security adapter, DTO mapping, REST exception handling.
- `application/outbound-repository-jpa`
  JPA adapter, Spring Data repository, persistence model, XML ORM mapping.
- `application/springboot`
  Composition root, configuration, Liquibase and application startup.

This split follows the same direction as the workshop examples:

- keep the domain clean
- keep adapters separate
- keep runtime assembly outside the domain

## Dependency Direction

The intended dependency direction is:

```text
inbound-controller-rest -> domain
outbound-repository-jpa -> domain
springboot -> domain + inbound-controller-rest + outbound-repository-jpa + api-spec
```

Important rules:

- `domain` must not depend on Spring
- `domain` must not depend on JPA
- `domain` must not depend on REST DTOs
- `outbound` must not depend on `inbound`

## What Lives Where

### `application/domain`

Contains:

- domain entities, repositories, and value objects under `com.hotel.management.domain.*`
- use case orchestration and ports under `com.hotel.management.service.*`
- service exceptions under `com.hotel.management.service.exception`
- domain exceptions and value objects under `com.hotel.management.domain.shared.*`
- security-neutral user abstraction `AuthenticatedUser`
- facade interfaces:
  - `ReservationFacade`
  - `SearchAvailabilityFacade`
  - `StaffReservationFacade`

The domain/service layer currently owns:

- reservation creation rules
- reservation ownership checks
- cancellation behavior
- room availability calculation
- stay operation orchestration
- use case orchestration

### `application/api-spec`

Contains the OpenAPI contract:

- [application/api-spec/src/main/resources/openapi/hotel-reservation.yaml](./application/api-spec/src/main/resources/openapi/hotel-reservation.yaml)

Generated from it:

- Spring API interface
- request DTOs
- response DTOs

This keeps the HTTP contract explicit and versionable.

### `application/inbound-controller-rest`

Contains:

- `ReservationsController`
- `ReservationMapper`
- JWT security configuration
- JWT converter and current user adapter
- REST exception handling

Responsibilities:

- receive HTTP requests
- map DTO -> use case input
- call the domain/service layer
- map result -> HTTP response

Controllers stay thin by design.

### `application/outbound-repository-jpa`

Contains:

- `ReservationJpaEntity`
- `ReservationSpringDataRepository`
- `JpaReservationRepositoryAdapter`
- XML ORM mapping

Responsibilities:

- store and load reservation data
- map persistence model <-> domain model
- contain JPA-only concerns

JPA entities are not used as domain entities.

### `application/springboot`

Contains:

- `HotelReservationApplication`
- bean wiring
- application properties
- Liquibase changelog

This module is the composition root.
It should not contain business logic.

## Current Reservation Slice

The current request flow is:

1. authenticated user sends HTTP request
2. REST controller accepts OpenAPI-generated DTO
3. mapper converts DTO to use case input
4. a facade interface is invoked from the REST layer
5. service implementation orchestrates domain rules and ports
6. JPA adapters persist and query PostgreSQL
7. result is mapped back to an API response

Implemented endpoints:

- `GET /rooms/search`
- `POST /reservations`
- `GET /reservations`
- `GET /reservations/{reservationId}`
- `POST /reservations/{reservationId}/cancel`
- `POST /staff/reservations/{reservationId}/check-in`
- `POST /staff/reservations/{reservationId}/check-out`

## Domain Design Decisions

### Reservation as an Entity

`Reservation` is modeled as a domain entity, not a JPA entity and not a raw DTO.

Current aggregate responsibilities:

- validate creation invariants
- ensure valid stay period
- ensure valid `GuestCount`
- enforce consistency between `status` and `cancelledAt`
- expose domain behavior for cancellation

### Ownership and Security

Ownership is checked in the service layer:

- normal user can access only own reservation
- admin can access any reservation

The aggregate knows how to verify ownership by `actorId`.
The decision that `ADMIN` can bypass ownership stays outside the aggregate.

This keeps access-control policy cleaner than embedding role logic directly in the domain entity.

## Persistence Design

The persistence model is intentionally separate from the domain model.

Current persistence choices:

- PostgreSQL as the main database
- Liquibase for schema evolution
- XML ORM mapping for cleaner separation from the domain layer

The `reservations` table currently stores:

- reservation identity
- hotel and room type references
- stay dates
- guest count
- status
- created timestamp
- cancelled timestamp
- creator identity

Database constraints currently reinforce:

- allowed reservation status values
- consistency between `status` and `cancelled_at`

## Security Design

The application is configured as a JWT resource server.

Current setup:

- Keycloak as token issuer
- JWT validation via issuer + JWK set
- current user extracted through an adapter
- roles mapped from JWT claims

Security is split into two levels:

1. endpoint-level access in Spring Security
2. ownership/business access in the service layer

This is intentional. Endpoint access and business ownership are not the same concern.

## Why `application/domain`

The project keeps `domain.*` and `service.*` in one Maven module on purpose.

Reasons:

- it keeps the module graph simple for an academic modular monolith
- it still keeps code separation explicit through packages
- inbound and outbound adapters depend on one stable domain module
- Spring, REST, and JPA stay outside this module

Inside the module the rule is:

- `com.hotel.management.domain.*` contains entities, value objects, repositories, and domain exceptions
- `com.hotel.management.service.*` contains facades, services, ports, security abstractions, and service exceptions

## Why This Architecture Is Enough for the First Submission

The assignment asks for:

- at least one secured REST endpoint
- JPA persistence, ideally with XML mapping
- communication with the domain layer
- hexagonal architecture
- OpenAPI documentation and generated interface

The current project now goes beyond the original first slice, but still keeps the same architectural shape: modular monolith, API-first contract, clean core, inbound REST adapters, and outbound JPA adapters.

## Next Natural Extensions

The current structure is ready to grow with additional business modules, for example:

- `hotel`
- `room`
- `guest`
- `staff-operations`
- `service-offering`
- `audit`

But those modules should be added only when their use cases are implemented.
The project should not create empty architecture shells in advance.
