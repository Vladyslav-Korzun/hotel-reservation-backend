# Hotel Reservation Architecture

## Goal

Build the backend as a modular monolith with hexagonal architecture and API-first workflow.

The system is intended for a hotel network, not for a single hotel CRUD application.

## Technology Baseline

- Java 25
- Maven multi-module build
- Spring Boot as runtime and wiring layer
- PostgreSQL as primary database
- Liquibase for schema migrations
- OpenAPI-first contract generation

Reference structure for modularization and layering:
`C:\Users\Vlad\Desktop\start\Пример\FSA-hexagonal-architecture-master`

The reference project is used as a structural example for module boundaries and hexagonal composition.
Its domain is not reused directly.

## Architectural Formula

`Modular Monolith + Hexagonal Architecture + API-first + Domain-centered design`

## Core Rules

1. Domain does not depend on Spring, JPA, REST, Spring Security, or external SDKs.
2. Controllers only handle transport concerns: request parsing, validation, mapping, response shaping.
3. Business rules live in domain and application use cases, not in controllers or JPA entities.
4. External dependencies are accessed only through ports.
5. Spring Boot is composition and runtime infrastructure, not business logic.

## Maven Modules

All runtime modules live under `application/`.

- `application/domain`
  Domain entities, value objects, enums, exceptions, ports, use cases, facades, domain services.
- `application/api-spec`
  OpenAPI contract and generated API interfaces and DTOs.
- `application/inbound-controller-rest`
  REST controllers, transport validation, request/response mappers, exception handling, security entry layer.
- `application/outbound-repository-jpa`
  JPA entities, Spring Data repositories, persistence adapters, persistence-to-domain mapping.
- `application/outbound-integration`
  Adapters for notifications, payments, loyalty integrations, holiday calendar, and other external systems.
- `application/springboot`
  Boot application, wiring, security configuration, profiles, Liquibase, runtime setup.

## Domain Modules

The domain is split conceptually into these business modules:

- `hotel`
- `room`
- `reservation`
- `service-offering`
- `staff-operations`
- `guest`
- `pricing`
- `loyalty`
- `audit`
- `user-access`

This split is mandatory at package and use-case level, even if everything still ships as one deployable artifact.

## MVP Scope

Initial release should cover:

- hotel network and hotel catalog
- room types and rooms
- search for available rooms
- reservation creation, retrieval, cancellation
- hotel service offerings and reservation service items
- staff check-in and check-out
- room status operations
- guest, staff, admin roles
- audit trail for key actions

Not in MVP:

- advanced loyalty engine
- payment gateway
- promo codes
- complex seasonal pricing engine
- Kafka and microservices
- event sourcing

## Key Aggregates

- `Hotel`
- `Room`
- `RoomType`
- `Guest`
- `Reservation`
- `ReservationServiceItem`
- `HotelServiceOffering`
- `Stay`
- `LoyaltyProfile`
- `AuditLogEntry`

## Required Value Objects

- `StayPeriod`
- `Money`
- `GuestCount`
- `RoomCapacity`
- `EmailAddress`
- `DateRange`

## Critical Invariants

- reservation period must be valid
- reservation must not exceed room capacity
- room in `MAINTENANCE` or `OUT_OF_SERVICE` cannot be reserved
- active reservations must not overlap
- cancelled reservation cannot be checked in
- check-out cannot happen before check-in
- hotel service offering must belong to the selected hotel
- room cannot become `AVAILABLE` while stay is active
- after check-out room normally moves to `CLEANING`
- important staff and admin actions must be audited

## Ports

Repository ports:

- `HotelRepository`
- `RoomRepository`
- `RoomTypeRepository`
- `GuestRepository`
- `ReservationRepository`
- `StayRepository`
- `HotelServiceOfferingRepository`
- `LoyaltyRepository`

Integration ports:

- `CurrentUserPort`
- `ClockPort`
- `NotificationPort`
- `PaymentPort`
- `HolidayCalendarPort`
- `AuditLogPort`

## First Use Cases

- `HotelQueryFacade`
- `SearchAvailabilityFacade`
- `ReservationFacade`
- `StaffReservationFacade`
- `RoomOperationsFacade`

## Packaging Direction

Recommended package root:
`com.hotel.reservation`

Recommended first-level package split in `domain`:

- `com.hotel.reservation.hotel`
- `com.hotel.reservation.room`
- `com.hotel.reservation.reservation`
- `com.hotel.reservation.serviceoffering`
- `com.hotel.reservation.staffoperations`
- `com.hotel.reservation.guest`
- `com.hotel.reservation.pricing`
- `com.hotel.reservation.loyalty`
- `com.hotel.reservation.audit`
- `com.hotel.reservation.useraccess`
- `com.hotel.reservation.shared`

`shared` is only for genuinely cross-cutting abstractions such as base exceptions, IDs, and reusable value objects.

## Delivery Order

1. lock glossary and package boundaries
2. implement domain model and ports
3. implement core use cases
4. lock OpenAPI contract
5. implement REST adapter
6. implement JPA adapter and Liquibase
7. implement security
8. add service offerings
9. add audit trail
10. add loyalty v1
11. add architecture tests
