package com.hotel.management;

import com.hotel.management.service.availability.SearchAvailableRoomsCommand;
import com.hotel.management.service.availability.SearchAvailabilityFacade;
import com.hotel.management.jpa.hotel.JpaHotelEntity;
import com.hotel.management.jpa.hotel.JpaHotelSpringDataRepository;
import com.hotel.management.jpa.reservation.JpaReservationSpringDataRepository;
import com.hotel.management.jpa.room.JpaRoomEntity;
import com.hotel.management.jpa.room.JpaRoomSpringDataRepository;
import com.hotel.management.jpa.room.JpaRoomTypeEntity;
import com.hotel.management.jpa.room.JpaRoomTypeSpringDataRepository;
import com.hotel.management.domain.shared.value.AccommodationParty;
import com.hotel.management.domain.shared.value.GuestComposition;
import com.hotel.management.domain.shared.value.StayPeriod;
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
@Testcontainers
class CreateReservationFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("hotel_reservation_test")
            .withUsername("hotel_user")
            .withPassword("hotel_password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaReservationSpringDataRepository reservationRepository;

    @Autowired
    private JpaHotelSpringDataRepository hotelRepository;

    @Autowired
    private JpaRoomTypeSpringDataRepository roomTypeRepository;

    @Autowired
    private JpaRoomSpringDataRepository roomRepository;

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
        reservationRepository.deleteAll();
        roomRepository.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();
        seedDefaultCatalog();
    }

    @Test
    void shouldCreateReservationThroughSecuredEndpoint() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-demo", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdBy").value("guest-demo"))
                .andReturn();

        assertThat(reservationRepository.count()).isEqualTo(1);
        var savedReservation = reservationRepository.findAll().getFirst();
        assertThat(savedReservation.getHotelId()).isEqualTo(1L);
        assertThat(savedReservation.getCreatedBy()).isEqualTo("guest-demo");
        assertThat(savedReservation.getAdultsCount()).isEqualTo(2);

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("guest-demo", "GUEST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(savedReservation.getId()))
                .andExpect(jsonPath("$.hotelId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/reservations/{reservationId}/cancel", savedReservation.getId())
                        .with(jwtFor("guest-demo", "GUEST")))
                .andExpect(status().isNoContent());

        var cancelledReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();
        assertThat(cancelledReservation.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelledReservation.getCancelledAt()).isNotNull();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("guest-demo", "GUEST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").isNotEmpty());
    }

    @Test
    void shouldRejectAccessToReservationOwnedByAnotherUser() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("owner-user", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isCreated());

        var savedReservation = reservationRepository.findAll().getFirst();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("other-user", "GUEST")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/reservations/{reservationId}/cancel", savedReservation.getId())
                        .with(jwtFor("other-user", "GUEST")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessAnotherUsersReservation() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("owner-user", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isCreated());

        var savedReservation = reservationRepository.findAll().getFirst();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("admin-user", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowStaffToAccessAnotherUsersReservation() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("owner-user", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isCreated());

        var savedReservation = reservationRepository.findAll().getFirst();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowStaffToCheckInAndCheckOutReservation() throws Exception {
        var checkIn = LocalDate.now(ZoneOffset.UTC);
        var checkOut = checkIn.plusDays(2);

        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-demo", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "%s",
                                  "checkOut": "%s",
                                  "adults": 2
                                }
                                """.formatted(checkIn, checkOut)))
                .andExpect(status().isCreated());

        var savedReservation = reservationRepository.findAll().getFirst();

        mockMvc.perform(post("/staff/reservations/{reservationId}/check-in", savedReservation.getId())
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.roomId").value(10));

        var checkedInReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();
        assertThat(checkedInReservation.getStatus()).isEqualTo("CHECKED_IN");
        assertThat(checkedInReservation.getRoomId()).isEqualTo(10L);
        assertThat(roomRepository.findById(10L).orElseThrow().getStatus()).isEqualTo("OCCUPIED");

        mockMvc.perform(post("/staff/reservations/{reservationId}/check-out", savedReservation.getId())
                        .with(jwtFor("staff-user", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"))
                .andExpect(jsonPath("$.roomId").value(10));

        var checkedOutReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();
        assertThat(checkedOutReservation.getStatus()).isEqualTo("CHECKED_OUT");
        assertThat(roomRepository.findById(10L).orElseThrow().getStatus()).isEqualTo("CLEANING");
    }

    @Test
    void shouldAllowStaffAndAdminToListAllReservations() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-one", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 1,
                                  "roomTypeId": 2,
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-two", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 5,
                                  "roomTypeId": 7,
                                  "checkIn": "2026-06-01",
                                  "checkOut": "2026-06-03",
                                  "adults": 1
                                }
                                """))
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
        hotelRepository.save(hotel(9L, "Danube Hotel", "Bratislava"));
        roomTypeRepository.save(roomType(10L, 9L, "Standard", 2));
        roomRepository.save(room(100L, 9L, "101", 10L, 2, "AVAILABLE"));
        roomRepository.save(room(101L, 9L, "102", 10L, 2, "AVAILABLE"));
        roomRepository.save(room(102L, 9L, "103", 10L, 2, "MAINTENANCE"));

        mockMvc.perform(post("/reservations")
                        .with(jwtFor("guest-one", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hotelId": 9,
                                  "roomTypeId": 10,
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isCreated());

        var result = searchAvailabilityFacade.searchAvailableRooms(SearchAvailableRoomsCommand.byCity(
                "Bratislava",
                new StayPeriod(LocalDate.parse("2026-05-10"), LocalDate.parse("2026-05-12")),
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
                                  "checkIn": "2026-05-10",
                                  "checkOut": "2026-05-12",
                                  "adults": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hotelId").value(9))
                .andExpect(jsonPath("$[0].roomTypeId").value(10))
                .andExpect(jsonPath("$[0].availableCount").value(1));
    }

    private void seedDefaultCatalog() {
        hotelRepository.save(hotel(1L, "Default Hotel", "Kosice"));
        roomTypeRepository.save(roomType(2L, 1L, "Standard", 2));
        roomRepository.save(room(10L, 1L, "101", 2L, 2, "AVAILABLE"));

        hotelRepository.save(hotel(5L, "Second Hotel", "Zilina"));
        roomTypeRepository.save(roomType(7L, 5L, "Single", 1));
        roomRepository.save(room(50L, 5L, "201", 7L, 1, "AVAILABLE"));
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtFor(
            String username,
            String role
    ) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject(username + "-sub")
                        .claim("preferred_username", username)
                        .claim("roles", List.of(role)))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
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
        hotel.setChildMaxAge(11);
        return hotel;
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
