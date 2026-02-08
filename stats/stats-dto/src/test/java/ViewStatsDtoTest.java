import dto.ViewStatsDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewStatsDtoTest {

    @Test
    void shouldCreateWithBuilder() {
        ViewStatsDto dto = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(150L)
                .build();

        assertEquals("ewm-main-service", dto.getApp());
        assertEquals("/events/1", dto.getUri());
        assertEquals(150L, dto.getHits());
    }

    @Test
    void shouldCreateWithConstructor() {
        ViewStatsDto dto = new ViewStatsDto("ewm-main-service", "/events/1", 150L);

        assertEquals("ewm-main-service", dto.getApp());
        assertEquals("/events/1", dto.getUri());
        assertEquals(150L, dto.getHits());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        ViewStatsDto dto1 = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(150L)
                .build();

        ViewStatsDto dto2 = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(150L)
                .build();

        ViewStatsDto dto3 = ViewStatsDto.builder()
                .app("different-app")
                .uri("/events/1")
                .hits(150L)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void shouldWorkToString() {
        ViewStatsDto dto = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(150L)
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("ewm-main-service"));
        assertTrue(toString.contains("/events/1"));
        assertTrue(toString.contains("150"));
    }

    @Test
    void shouldWorkSettersAndGetters() {
        ViewStatsDto dto = new ViewStatsDto();
        dto.setApp("ewm-main-service");
        dto.setUri("/events/1");
        dto.setHits(150L);

        assertEquals("ewm-main-service", dto.getApp());
        assertEquals("/events/1", dto.getUri());
        assertEquals(150L, dto.getHits());
    }
}