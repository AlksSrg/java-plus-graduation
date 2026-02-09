import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ViewStatsDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import ru.practicum.StatsClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

class StatsClientTest {
    private MockWebServer mockWebServer;
    private StatsClient statsClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void beforeEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        RetryTemplate retryTemplate = new RetryTemplateBuilder()
                .maxAttempts(1)
                .build();

        statsClient = new StatsClient(baseUrl, retryTemplate);
    }

    @Test
    public void testMethodSaveStat() throws IOException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        boolean result = statsClient.saveStat("ewm-main-service", "/events", "127.0.0.1");
        Assertions.assertTrue(result);

        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        result = statsClient.saveStat("ewm-main-service", "/events", "127.0.0.1");
        Assertions.assertFalse(result);

        result = statsClient.saveStat("", "/events", "127.0.0.1");
        Assertions.assertFalse(result);
    }

    @Test
    public void testMethodGetStats() throws IOException {
        List<ViewStatsDto> listViewStateDto = List.of(
                ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri("/events/1")
                        .hits(50L)
                        .build(),
                ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri("/events/100")
                        .hits(200L)
                        .build()
        );
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(listViewStateDto))
                .addHeader("Content-Type", "application/json"));

        List<ViewStatsDto> result = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                true
        );

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("ewm-main-service", result.getFirst().getApp());
        Assertions.assertEquals("/events/1", result.getFirst().getUri());
        Assertions.assertEquals(50L, result.getFirst().getHits());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(Collections.singletonList(listViewStateDto.getFirst())))
                .addHeader("Content-Type", "application/json"));

        result = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                List.of("/events/1"),
                true
        );

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("/events/1", result.getFirst().getUri());

        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        result = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                true
        );

        Assertions.assertTrue(result.isEmpty());
        result = statsClient.getStats(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1),
                true
        );

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyResponse() throws IOException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        List<ViewStatsDto> result = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                true
        );

        Assertions.assertTrue(result.isEmpty());
    }

    @AfterEach
    public void afterEach() throws IOException {
        mockWebServer.shutdown();
    }
}