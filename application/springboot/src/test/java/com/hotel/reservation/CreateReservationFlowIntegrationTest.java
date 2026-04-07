package com.hotel.reservation;

import com.hotel.reservation.jpa.ReservationSpringDataRepository;
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
    private ReservationSpringDataRepository reservationRepository;

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
                                  "guestCount": 2
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
                                  "guestCount": 2
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
                                  "guestCount": 2
                                }
                                """))
                .andExpect(status().isCreated());

        var savedReservation = reservationRepository.findAll().getFirst();

        mockMvc.perform(get("/reservations/{reservationId}", savedReservation.getId())
                        .with(jwtFor("admin-user", "ADMIN")))
                .andExpect(status().isOk());
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
}
