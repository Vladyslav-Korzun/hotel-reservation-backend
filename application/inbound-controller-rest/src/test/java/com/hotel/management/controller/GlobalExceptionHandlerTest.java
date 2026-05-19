package com.hotel.management.controller;

import com.hotel.management.domain.shared.exception.NotFoundException;
import com.hotel.management.domain.shared.exception.ValidationException;
import com.hotel.management.domain.shared.exception.ForbiddenException;
import com.hotel.management.domain.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/reservations/1");

    @Test
    void shouldMapValidationExceptionToBadRequest() {
        var problem = handler.handleValidationException(new ValidationException("invalid stay period"), request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Validation failed");
        assertThat(problem.getDetail()).isEqualTo("invalid stay period");
        assertThat(problem.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problem.getInstance()).isEqualTo(URI.create("/reservations/1"));
    }

    @Test
    void shouldMapNotFoundExceptionToNotFound() {
        var problem = handler.handleNotFoundException(new NotFoundException("reservation not found"), request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Not found");
        assertThat(problem.getDetail()).isEqualTo("reservation not found");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/reservations/1"));
    }

    @Test
    void shouldMapUnauthorizedExceptionToUnauthorized() {
        var problem = handler.handleUnauthorizedException(new UnauthorizedException("token is required"), request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getTitle()).isEqualTo("Authentication required");
        assertThat(problem.getDetail()).isEqualTo("token is required");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/reservations/1"));
    }

    @Test
    void shouldMapForbiddenExceptionToForbidden() {
        var problem = handler.handleForbiddenException(new ForbiddenException("staff role is required"), request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problem.getTitle()).isEqualTo("Access denied");
        assertThat(problem.getDetail()).isEqualTo("staff role is required");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/reservations/1"));
    }
}
