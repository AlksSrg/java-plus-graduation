import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ViewStatsDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.StatsClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

class StatsClientTest {
    private MockWebServer mockWebServer;
    private StatsClient statsClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void beforeEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        statsClient = new StatsClient(mockWebServer.url("/").toString());
    }

    @Test
    public void testMethodSaveStat() throws IOException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(201));
        boolean result = statsClient.saveStat("ewm-main-service", "/events", "127.0.0.1");
        Assertions.assertTrue(result);

        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        result = statsClient.saveStat("ewm-main-service", "/events", "127.0.0.1");
        Assertions.assertFalse(result);

        mockWebServer.shutdown();
        result = statsClient.saveStat("ewm-main-service", "/events", "127.0.0.1");
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
        List<ViewStatsDto> result = statsClient.getStats(LocalDateTime.now(),
                LocalDateTime.now().plusDays(5), true);
        Assertions.assertEquals(2, result.size());

        ViewStatsDto viewStatsDto = listViewStateDto.getFirst();
        Assertions.assertEquals(viewStatsDto.getApp(), result.getFirst().getApp());
        Assertions.assertEquals(viewStatsDto.getUri(), result.getFirst().getUri());
        Assertions.assertEquals(viewStatsDto.getHits(), result.getFirst().getHits());

        viewStatsDto = listViewStateDto.get(1);
        Assertions.assertEquals(viewStatsDto.getApp(), result.get(1).getApp());
        Assertions.assertEquals(viewStatsDto.getUri(), result.get(1).getUri());
        Assertions.assertEquals(viewStatsDto.getHits(), result.get(1).getHits());

        mockWebServer.shutdown();
        result = statsClient.getStats(LocalDateTime.now(),
                LocalDateTime.now().plusDays(5), true);
        Assertions.assertEquals(0, result.size());

    }

    @AfterEach
    public void afterEach() throws IOException {
        mockWebServer.shutdown();
    }
}