package com.hotel.management;

import com.hotel.management.domain.audit.AuditActionType;
import com.hotel.management.domain.audit.AuditEntityType;
import com.hotel.management.domain.audit.AuditLogEntry;
import com.hotel.management.domain.guest.Guest;
import com.hotel.management.domain.guest.GuestRepository;
import com.hotel.management.domain.hotel.Hotel;
import com.hotel.management.domain.hotel.HotelPolicy;
import com.hotel.management.domain.hotel.HotelRepository;
import com.hotel.management.domain.hotel.HotelStatus;
import com.hotel.management.domain.reservation.Reservation;
import com.hotel.management.domain.reservation.ReservationPriceSnapshot;
import com.hotel.management.domain.reservation.ReservationRepository;
import com.hotel.management.domain.reservation.ReservationServiceItem;
import com.hotel.management.domain.reservation.ReservationStatus;
import com.hotel.management.domain.room.OccupancyPolicy;
import com.hotel.management.domain.room.PetPolicy;
import com.hotel.management.domain.room.Room;
import com.hotel.management.domain.room.RoomRepository;
import com.hotel.management.domain.room.RoomStatus;
import com.hotel.management.domain.room.RoomType;
import com.hotel.management.domain.room.RoomTypeFeatures;
import com.hotel.management.domain.room.RoomTypeRepository;
import com.hotel.management.domain.serviceoffering.ServiceOffering;
import com.hotel.management.domain.serviceoffering.ServiceOfferingRepository;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.EmailAddress;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.Money;
import com.hotel.management.domain.stay.Stay;
import com.hotel.management.domain.stay.StayRepository;
import com.hotel.management.domain.stay.StayStatus;
import com.hotel.management.jpa.audit.JpaAuditLogEntity;
import com.hotel.management.domain.audit.AuditLogPort;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class JpaRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("hotel_reservation_adapter_test")
            .withUsername("hotel_user")
            .withPassword("hotel_password");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ServiceOfferingRepository serviceOfferingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AuditLogPort auditLogPort;

    @Autowired
    private StayRepository stayRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @BeforeEach
    void setUp() {
        entityManager.createQuery("delete from JpaAuditLogEntity").executeUpdate();
        entityManager.createQuery("delete from JpaStayEntity").executeUpdate();
        entityManager.createQuery("delete from JpaReservationServiceItemEntity").executeUpdate();
        entityManager.createQuery("delete from JpaReservationEntity").executeUpdate();
        entityManager.createQuery("delete from JpaServiceOfferingEntity").executeUpdate();
        entityManager.createQuery("delete from JpaRoomEntity").executeUpdate();
        entityManager.createQuery("delete from JpaRoomTypeEntity").executeUpdate();
        entityManager.createQuery("delete from JpaGuestEntity").executeUpdate();
        entityManager.createQuery("delete from JpaHotelEntity").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldPersistCatalogThroughRepositoryPorts() {
        hotelRepository.save(hotel(1L, "Danube Hotel", "Bratislava", HotelStatus.ACTIVE));
        hotelRepository.save(hotel(2L, "Closed Hotel", "Bratislava", HotelStatus.INACTIVE));
        roomTypeRepository.save(roomType(10L, 1L, "Deluxe"));
        roomRepository.save(new Room(100L, 1L, "101", 10L, 3, RoomStatus.AVAILABLE));
        roomRepository.save(new Room(101L, 1L, "102", 10L, 3, RoomStatus.MAINTENANCE));
        serviceOfferingRepository.save(serviceOffering(1000L, 1L, "BREAKFAST", true));
        serviceOfferingRepository.save(serviceOffering(1001L, 1L, "SPA", false));

        entityManager.flush();
        entityManager.clear();

        assertThat(hotelRepository.findActiveByCity("Bratislava"))
                .extracting(Hotel::id)
                .containsExactly(1L);
        assertThat(roomTypeRepository.findByHotelIds(List.of(1L)))
                .extracting(RoomType::id)
                .containsExactly(10L);
        assertThat(roomRepository.findByHotelIds(List.of(1L)))
                .extracting(Room::id)
                .containsExactlyInAnyOrder(100L, 101L);
        assertThat(serviceOfferingRepository.findActiveByHotelId(1L))
                .extracting(ServiceOffering::id)
                .containsExactly(1000L);
    }

    @Test
    void shouldPersistGuestThroughRepositoryPort() {
        guestRepository.save(guest(10L, "john.smith@example.com"));

        entityManager.flush();
        entityManager.clear();

        var saved = guestRepository.findById(10L).orElseThrow();
        assertThat(saved.firstName()).isEqualTo("John");
        assertThat(saved.email()).isEqualTo(new EmailAddress("john.smith@example.com"));
        assertThat(guestRepository.findByEmail(new EmailAddress("JOHN.SMITH@example.com"))).isPresent();
    }

    @Test
    void shouldPersistReservationSnapshotAndServiceItemsThroughRepositoryPort() {
        guestRepository.save(guest(10L, "reservation.guest@example.com"));
        hotelRepository.save(hotel(1L, "Danube Hotel", "Bratislava", HotelStatus.ACTIVE));
        roomTypeRepository.save(roomType(10L, 1L, "Deluxe"));
        roomRepository.save(new Room(100L, 1L, "101", 10L, 3, RoomStatus.AVAILABLE));
        serviceOfferingRepository.save(serviceOffering(1000L, 1L, "BREAKFAST", true));

        var reservation = Reservation.createPending(
                "reservation-1",
                1L,
                10L,
                10L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                new AccommodationParty(new GuestComposition(2, List.of(7)), List.of()),
                new ReservationPriceSnapshot(
                        Money.of("200.00", "EUR"),
                        Money.of("30.00", "EUR"),
                        Money.of("0.00", "EUR"),
                        Money.of("230.00", "EUR")
                ),
                List.of(new ReservationServiceItem(
                        null,
                        "reservation-1",
                        1000L,
                        "Breakfast",
                        Money.of("15.00", "EUR"),
                        2
                )),
                Instant.parse("2026-05-10T10:00:00Z"),
                "guest-1"
        );

        reservationRepository.save(reservation);
        entityManager.flush();
        entityManager.clear();

        var saved = reservationRepository.findById("reservation-1").orElseThrow();
        assertThat(saved.hotelId()).isEqualTo(1L);
        assertThat(saved.accommodationParty().guests().childrenAges()).containsExactly(7);
        assertThat(saved.basePrice().amount()).isEqualByComparingTo("200.00");
        assertThat(saved.servicesPrice().amount()).isEqualByComparingTo("30.00");
        assertThat(saved.finalPrice().amount()).isEqualByComparingTo("230.00");
        assertThat(saved.serviceItems()).hasSize(1);
        assertThat(saved.serviceItems().getFirst().id()).isNotNull();
        assertThat(saved.serviceItems().getFirst().serviceOfferingId()).isEqualTo(1000L);
        assertThat(saved.serviceItems().getFirst().totalPrice().amount()).isEqualByComparingTo("30.00");

        assertThat(reservationRepository.findByCreatedBy("guest-1", 10))
                .extracting(Reservation::id)
                .containsExactly("reservation-1");
    }

    @Test
    void shouldAppendAuditLogThroughPort() {
        auditLogPort.append(new AuditLogEntry(
                null,
                "admin-1",
                "ADMIN",
                AuditActionType.CREATE_HOTEL,
                AuditEntityType.HOTEL,
                "1",
                Instant.parse("2026-05-10T12:00:00Z"),
                "created hotel"
        ));

        entityManager.flush();
        entityManager.clear();

        var entries = entityManager.createQuery(
                        "select entry from JpaAuditLogEntity entry order by entry.id",
                        JpaAuditLogEntity.class
                )
                .getResultList();

        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getId()).isNotNull();
        assertThat(entries.getFirst().getActorId()).isEqualTo("admin-1");
        assertThat(entries.getFirst().getActorRole()).isEqualTo("ADMIN");
        assertThat(entries.getFirst().getActionType()).isEqualTo("CREATE_HOTEL");
        assertThat(entries.getFirst().getEntityType()).isEqualTo("HOTEL");
        assertThat(entries.getFirst().getEntityId()).isEqualTo("1");
        assertThat(entries.getFirst().getDetails()).isEqualTo("created hotel");
    }

    @Test
    void shouldPersistStayThroughRepositoryPort() {
        guestRepository.save(guest(10L, "stay.guest@example.com"));
        hotelRepository.save(hotel(1L, "Danube Hotel", "Bratislava", HotelStatus.ACTIVE));
        roomTypeRepository.save(roomType(10L, 1L, "Deluxe"));
        roomRepository.save(new Room(100L, 1L, "101", 10L, 3, RoomStatus.OCCUPIED));
        reservationRepository.save(Reservation.rehydrate(
                "reservation-1",
                1L,
                10L,
                100L,
                10L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                new AccommodationParty(new GuestComposition(2, List.of()), List.of()),
                priceSnapshot(),
                List.of(),
                ReservationStatus.CHECKED_IN,
                Instant.parse("2026-05-10T10:00:00Z"),
                null,
                "guest-1"
        ));

        var activeStay = stayRepository.save(Stay.start(
                null,
                "reservation-1",
                100L,
                Instant.parse("2026-06-01T14:00:00Z")
        ));
        entityManager.flush();
        entityManager.clear();

        var savedActiveStay = stayRepository.findActiveByReservationId("reservation-1").orElseThrow();
        assertThat(savedActiveStay.id()).isNotNull();
        assertThat(savedActiveStay.roomId()).isEqualTo(100L);
        assertThat(savedActiveStay.status()).isEqualTo(StayStatus.ACTIVE);

        stayRepository.save(activeStay.complete(Instant.parse("2026-06-03T11:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        assertThat(stayRepository.findActiveByReservationId("reservation-1")).isEmpty();
    }

    private static Hotel hotel(Long id, String name, String city, HotelStatus status) {
        return new Hotel(
                id,
                name,
                city,
                "Slovakia",
                "Main street 1",
                4,
                "City hotel",
                status,
                new HotelPolicy(true, true, 2, 12, 13)
        );
    }

    private static Guest guest(Long id, String email) {
        return new Guest(
                id,
                "John",
                "Smith",
                new EmailAddress(email),
                "+421900000000"
        );
    }

    private static RoomType roomType(Long id, Long hotelId, String name) {
        return new RoomType(
                id,
                hotelId,
                name,
                new OccupancyPolicy(2, 1, 1, 3),
                new PetPolicy(false, 0, Set.of(), null, null),
                Money.of("100.00", "EUR"),
                name + " room",
                RoomTypeFeatures.empty()
        );
    }

    private static ServiceOffering serviceOffering(Long id, Long hotelId, String code, boolean active) {
        return new ServiceOffering(
                id,
                hotelId,
                code,
                code.charAt(0) + code.substring(1).toLowerCase(),
                code + " service",
                Money.of("15.00", "EUR"),
                active,
                "DAILY"
        );
    }

    private static ReservationPriceSnapshot priceSnapshot() {
        Money zero = Money.zero("EUR");
        return new ReservationPriceSnapshot(zero, zero, zero, zero);
    }
}