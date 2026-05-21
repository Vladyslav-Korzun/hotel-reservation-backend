package com.hotel.management;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class EndpointSecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("hotel_reservation_security_test")
            .withUsername("hotel_user")
            .withPassword("hotel_password");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Test
    void shouldAllowHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAvailabilityCalendarWithoutToken() throws Exception {
        mockMvc.perform(get("/hotels/1001/room-types/1102/availability-calendar")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowHotelListWithoutToken() throws Exception {
        mockMvc.perform(get("/hotels"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowHotelDetailsWithoutToken() throws Exception {
        mockMvc.perform(get("/hotels/1001"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowHotelServicesWithoutToken() throws Exception {
        mockMvc.perform(get("/hotels/1001/services"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowRoomSearchWithoutToken() throws Exception {
        mockMvc.perform(post("/rooms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectReservationCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowPublicReservationCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/public/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectGuestFromAdminEndpoints() throws Exception {
        mockMvc.perform(post("/admin/hotels")
                        .with(jwtFor("guest-user", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectStaffFromGuestReservationCreation() throws Exception {
        mockMvc.perform(post("/reservations")
                        .with(jwtFor("staff-user", "STAFF"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectGuestFromStaffReservationCreation() throws Exception {
        mockMvc.perform(post("/staff/reservations")
                        .with(jwtFor("guest-user", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectGuestFromListingAllReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                        .with(jwtFor("guest-user", "GUEST")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectGuestFromStaffReservationOperations() throws Exception {
        mockMvc.perform(post("/staff/reservations/reservation-1/check-in")
                        .with(jwtFor("guest-user", "GUEST")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectGuestFromRoomOperations() throws Exception {
        mockMvc.perform(patch("/staff/rooms/10/status")
                        .with(jwtFor("guest-user", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CLEANING"
                                }
                                """))
                .andExpect(status().isForbidden());
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
