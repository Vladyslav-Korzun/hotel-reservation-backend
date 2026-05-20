package com.hotel.management;

import com.hotel.management.domain.service.availability.SearchAvailableRoomsCommand;
import com.hotel.management.domain.service.availability.SearchAvailabilityFacade;
import com.hotel.management.jpa.guest.JpaGuestEntity;
import com.hotel.management.jpa.hotel.JpaHotelEntity;
import com.hotel.management.jpa.reservation.JpaReservationEntity;
import com.hotel.management.jpa.room.JpaRoomEntity;
import com.hotel.management.jpa.room.JpaRoomTypeEntity;
import com.hotel.management.jpa.stay.JpaStayEntity;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.StayPeriod;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class CreateReservationFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("hotel_reservation_test")
            .withUsername("hotel_user")
            .withPassword("hotel_password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SearchAvailabilityFacade searchAvailabilityFacade;

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
        seedDefaultCatalog();
        flushAndClear();
    }

    @Test
    void shouldCreateReservationThroughSecuredEndpoint() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-demo", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(1L, 2L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.stayingGuests.length()").value(2))
                .andExpect(jsonPath("$.adults").value(2))
                .andExpect(jsonPath("$.createdBy").value("guest-demo"))
                .andReturn();

        flushAndClear();
        assertThat(reservationCount()).isEqualTo(1);
        var savedReservation = firstReservation();
        assertThat(savedReservation.getHotelId()).isEqualTo(1L);
        assertThat(savedReservation.getCreatedBy()).isEqualTo("guest-demo");
        assertThat(savedReservation.getAdultsCount()).isEqualTo(2);
        assertThat(savedReservation.getStayingGuestsJson()).contains("Guest1");

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("guest-demo", "GUEST", 10L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(savedReservation.getId()))
                .andExpect(jsonPath("$.hotelId").value(1))
                .andExpect(jsonPath("$.stayingGuests.length()").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", savedReservation.getId())
                        .with(jwtFor("guest-demo", "GUEST", 10L)))
                .andExpect(status().isNoContent());

        flushAndClear();
        var cancelledReservation = reservationById(savedReservation.getId());
        assertThat(cancelledReservation.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelledReservation.getCancelledAt()).isNotNull();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("guest-demo", "GUEST", 10L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").isNotEmpty());
    }

    @Test
    void shouldRejectSelfBookingWhenLinkedGuestProfileDoesNotExist() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-demo", "GUEST", 99L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(1L, 2L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isNotFound());

        flushAndClear();
        assertThat(reservationCount()).isZero();
    }

    @Test
    void shouldCreatePublicReservationAndCreateGuestProfile() throws Exception {
        mockMvc.perform(post("/public/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "firstName": "Anonymous",
                                  "lastName": "Guest",
                                  "email": "anonymous.guest@example.com",
                                  "phone": "+421900000099",
                                  "checkIn": "%s",
                                  "checkOut": "%s",
                                  "stayingGuests": %s
                                }
                                """.formatted(futureCheckIn(), futureCheckOut(), stayingGuests(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.guestId").isNumber())
                .andExpect(jsonPath("$.createdBy").isNotEmpty());

        flushAndClear();
        assertThat(reservationCount()).isEqualTo(1);
        var savedReservation = firstReservation();
        assertThat(savedReservation.getGuestId()).isNotNull();
        assertThat(savedReservation.getCreatedBy()).isEqualTo("public:" + savedReservation.getGuestId());
    }

    @Test
    void shouldRejectPublicReservationWhenCallerIsAuthenticated() throws Exception {
        mockMvc.perform(post("/public/reservations")
                        .with(jwtFor("guest-demo", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "firstName": "Demo",
                                  "lastName": "Guest",
                                  "email": "demo.public@example.com",
                                  "phone": "+421900000098",
                                  "checkIn": "%s",
                                  "checkOut": "%s",
                                  "stayingGuests": %s
                                }
                                """.formatted(futureCheckIn(), futureCheckOut(), stayingGuests(2))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldCreateStaffReservationForSelectedGuest() throws Exception {
        mockMvc.perform(post("/staff/reservations")
                        .with(jwtFor("staff-user", "STAFF"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "guestId": 10,
                                  "checkIn": "%s",
                                  "checkOut": "%s",
                                  "stayingGuests": %s
                                }
                                """.formatted(futureCheckIn(), futureCheckOut(), stayingGuests(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestId").value(10))
                .andExpect(jsonPath("$.createdBy").value("staff-user"));
    }

    @Test
    void shouldRejectAccessToReservationOwnedByAnotherUser() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("owner-user", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(1L, 2L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isCreated());

        flushAndClear();
        var savedReservation = firstReservation();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("other-user", "GUEST", 99L)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/reservations/{reservationId}/cancel", savedReservation.getId())
                        .with(jwtFor("other-user", "GUEST", 99L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessAnotherUsersReservation() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("owner-user", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(1L, 2L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isCreated());

        flushAndClear();
        var savedReservation = firstReservation();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("admin-user", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowStaffToAccessAnotherUsersReservation() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("owner-user", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(1L, 2L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isCreated());

        flushAndClear();
        var savedReservation = firstReservation();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowStaffToCheckInAndCheckOutReservation() throws Exception {
        var checkIn = LocalDate.now(ZoneOffset.UTC);
        var checkOut = checkIn.plusDays(2);

        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-demo", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "%s",
                                  "checkOut": "%s",
                                  "stayingGuests": %s
                                }
                                """.formatted(checkIn, checkOut, stayingGuests(2))))
                .andExpect(status().isCreated());

        flushAndClear();
        var savedReservation = firstReservation();

        mockMvc.perform(post("/staff/reservations/{reservationId}/check-in", savedReservation.getId())
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.roomId").value(10));

        flushAndClear();
        var checkedInReservation = reservationById(savedReservation.getId());
        assertThat(checkedInReservation.getStatus()).isEqualTo("CHECKED_IN");
        assertThat(checkedInReservation.getRoomId()).isEqualTo(10L);
        assertThat(roomById(10L).getStatus()).isEqualTo("OCCUPIED");
        var activeStay = stayByReservationId(savedReservation.getId());
        assertThat(activeStay.getRoomId()).isEqualTo(10L);
        assertThat(activeStay.getStatus()).isEqualTo("ACTIVE");
        assertThat(activeStay.getCheckedOutAt()).isNull();

        mockMvc.perform(post("/staff/reservations/{reservationId}/check-out", savedReservation.getId())
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"))
                .andExpect(jsonPath("$.roomId").value(10));

        flushAndClear();
        var checkedOutReservation = reservationById(savedReservation.getId());
        assertThat(checkedOutReservation.getStatus()).isEqualTo("CHECKED_OUT");
        assertThat(roomById(10L).getStatus()).isEqualTo("CLEANING");
        var completedStay = stayByReservationId(savedReservation.getId());
        assertThat(completedStay.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedStay.getCheckedOutAt()).isNotNull();
    }

    @Test
    void shouldAllowStaffAndAdminToListAllReservations() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-one", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(1L, 2L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-two", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(5L, 7L, futureCheckIn().plusDays(5), futureCheckOut().plusDays(5), 1)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/reservations")
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/reservations")
                        .with(jwtFor("admin-user", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRejectGuestListingAllReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                        .with(jwtFor("guest-demo", "GUEST")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSearchAvailableRoomsThroughJpaAdapters() throws Exception {
        save(hotel(9L, "Danube Hotel", "Bratislava"));
        save(roomType(10L, 9L, "Standard", 2));
        save(room(100L, 9L, "101", 10L, 2, "AVAILABLE"));
        save(room(101L, 9L, "102", 10L, 2, "AVAILABLE"));
        save(room(102L, 9L, "103", 10L, 2, "MAINTENANCE"));
        flushAndClear();

        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-one", "GUEST", 10L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(9L, 10L, futureCheckIn(), futureCheckOut(), 2)))
                .andExpect(status().isCreated());

        var result = searchAvailabilityFacade.searchAvailableRooms(SearchAvailableRoomsCommand.byCity(
                "Bratislava",
                new StayPeriod(futureCheckIn(), futureCheckOut()),
                party(2)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().hotelId()).isEqualTo(9L);
        assertThat(result.getFirst().roomTypeId()).isEqualTo(10L);
        assertThat(result.getFirst().availableCount()).isEqualTo(1);

        mockMvc.perform(post("/rooms/search")
                        .with(jwtFor("guest-one", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city": "Bratislava",
                                  "checkIn": "%s",
                                  "checkOut": "%s",
                                  "adults": 2
                                }
                                """.formatted(futureCheckIn(), futureCheckOut())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hotelId").value(9))
                .andExpect(jsonPath("$[0].roomTypeId").value(10))
                .andExpect(jsonPath("$[0].availableCount").value(1));
    }

    private void seedDefaultCatalog() {
        save(guest(10L, "guest@example.com"));

        save(hotel(1L, "Default Hotel", "Kosice"));
        save(roomType(2L, 1L, "Standard", 2));
        save(room(10L, 1L, "101", 2L, 2, "AVAILABLE"));

        save(hotel(5L, "Second Hotel", "Zilina"));
        save(roomType(7L, 5L, "Single", 1));
        save(room(50L, 5L, "201", 7L, 1, "AVAILABLE"));
    }

    private void save(Object entity) {
        entityManager.merge(entity);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private long reservationCount() {
        return entityManager.createQuery("select count(reservation) from JpaReservationEntity reservation", Long.class)
                .getSingleResult();
    }

    private JpaReservationEntity firstReservation() {
        return entityManager.createQuery(
                        "select reservation from JpaReservationEntity reservation order by reservation.createdAt desc",
                        JpaReservationEntity.class)
                .setMaxResults(1)
                .getSingleResult();
    }

    private JpaReservationEntity reservationById(String reservationId) {
        return entityManager.find(JpaReservationEntity.class, reservationId);
    }

    private JpaRoomEntity roomById(Long roomId) {
        return entityManager.find(JpaRoomEntity.class, roomId);
    }

    private JpaStayEntity stayByReservationId(String reservationId) {
        return entityManager.createQuery(
                        "select stay from JpaStayEntity stay where stay.reservationId = :reservationId",
                        JpaStayEntity.class)
                .setParameter("reservationId", reservationId)
                .getSingleResult();
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtFor(
            String username,
            String role
    ) {
        return jwtFor(username, role, null);
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtFor(
            String username,
            String role,
            Long guestId
    ) {
        return jwt()
                .jwt(jwt -> {
                    jwt.subject(username + "-sub")
                            .claim("preferred_username", username)
                            .claim("roles", List.of(role));
                    if (guestId != null) {
                        jwt.claim("guest_id", guestId);
                    }
                })
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static LocalDate futureCheckIn() {
        return LocalDate.now(ZoneOffset.UTC).plusDays(30);
    }

    private static LocalDate futureCheckOut() {
        return futureCheckIn().plusDays(2);
    }

    private static String reservationRequest(long hotelId, long roomTypeId, LocalDate checkIn, LocalDate checkOut, int adults) {
        return """
                {
                  "hotelId": %d,
                  "roomTypeId": %d,
                  "checkIn": "%s",
                  "checkOut": "%s",
                  "stayingGuests": %s
                }
                """.formatted(hotelId, roomTypeId, checkIn, checkOut, stayingGuests(adults));
    }

    private static String stayingGuests(int adults) {
        var guests = new StringBuilder("[");
        for (int index = 1; index <= adults; index++) {
            if (index > 1) {
                guests.append(",");
            }
            guests.append("""
                    {
                      "firstName": "Guest%s",
                      "lastName": "Tester",
                      "age": %d,
                      "gender": "OTHER"
                    }
                    """.formatted(index, 20 + index));
        }
        return guests.append("]").toString();
    }

    private static JpaHotelEntity hotel(Long id, String name, String city) {
        var hotel = new JpaHotelEntity();
        hotel.setId(id);
        hotel.setName(name);
        hotel.setCity(city);
        hotel.setCountry("Slovakia");
        hotel.setAddress("Main street 1");
        hotel.setStars(4);
        hotel.setDescription("City hotel");
        hotel.setStatus("ACTIVE");
        hotel.setChildrenAllowed(true);
        hotel.setPetsAllowed(true);
        hotel.setInfantMaxAge(2);
        hotel.setChildMaxAge(12);
        hotel.setAdultEquivalentAge(13);
        return hotel;
    }

    private static JpaGuestEntity guest(Long id, String email) {
        var guest = new JpaGuestEntity();
        guest.setId(id);
        guest.setFirstName("Demo");
        guest.setLastName("Guest");
        guest.setEmail(email);
        guest.setPhone("+421900000000");
        return guest;
    }

    private static JpaRoomTypeEntity roomType(Long id, Long hotelId, String name, int capacity) {
        var roomType = new JpaRoomTypeEntity();
        roomType.setId(id);
        roomType.setHotelId(hotelId);
        roomType.setName(name);
        roomType.setMaxAdults(capacity);
        roomType.setMaxChildren(0);
        roomType.setMaxInfants(0);
        roomType.setMaxTotalGuests(capacity);
        roomType.setPetsAllowed(false);
        roomType.setMaxPets(0);
        roomType.setAllowedPetTypesJson("[]");
        roomType.setBasePriceAmount(new BigDecimal("100.00"));
        roomType.setBasePriceCurrency("EUR");
        roomType.setDescription(name + " room");
        return roomType;
    }

    private static JpaRoomEntity room(Long id, Long hotelId, String number, Long roomTypeId, int capacity, String status) {
        var room = new JpaRoomEntity();
        room.setId(id);
        room.setHotelId(hotelId);
        room.setNumber(number);
        room.setRoomTypeId(roomTypeId);
        room.setCapacity(capacity);
        room.setStatus(status);
        return room;
    }

    private static AccommodationParty party(int adults, Integer... childrenAges) {
        return new AccommodationParty(new GuestComposition(adults, List.of(childrenAges)), List.of());
    }
}
