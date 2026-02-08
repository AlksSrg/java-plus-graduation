import dto.EndpointHitDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EndpointHitDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldCreateWithValidData() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        assertEquals("ewm-main-service", dto.getApp());
        assertEquals("/events/1", dto.getUri());
        assertEquals("192.168.1.1", dto.getIp());
        assertNotNull(dto.getTimestamp());
    }

    @Test
    void shouldValidateBlankApp() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        Set<ConstraintViolation<EndpointHitDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateInvalidIp() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("invalid-ip")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        Set<ConstraintViolation<EndpointHitDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateNullTimestamp() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(null)
                .build();

        Set<ConstraintViolation<EndpointHitDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);
        EndpointHitDto dto1 = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(timestamp)
                .build();

        EndpointHitDto dto2 = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(timestamp)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}