package ru.practicum;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class StatsClient {
    private final RestClient restClient;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-client.url:http://localhost:9090}") String uriBase) {
        this.restClient = RestClient.builder()
                .baseUrl(uriBase)
                .build();
    }

    /**
     * Сохраняет информацию о запросе к эндпоинту.
     *
     * @param app название сервиса
     * @param uri URI эндпоинта
     * @param ip  IP адрес пользователя
     * @return true если информация успешно сохранена, false в противном случае
     */
    public boolean saveStat(String app, String uri, String ip) {
        if (app == null || app.isBlank()
                || uri == null || uri.isBlank()
                || ip == null || ip.isBlank()) {
            log.error("Некорректные входные параметры: app - {}, uri - {}, api - {}", app, uri, ip);
            return false;
        }

        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri("/hit")
                    .contentType(APPLICATION_JSON)
                    .body(endpointHit)
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (ResourceAccessException ex) {
            log.error("Сервер не доступен");
            return false;
        } catch (RestClientException ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Получает статистику просмотров за указанный период.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param unique учитывать только уникальные посещения
     * @return список статистики просмотров
     */
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, boolean unique) {
        return getStats(start, end, null, unique);
    }

    /**
     * Получает статистику просмотров за указанный период для конкретных URI.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список URI для фильтрации
     * @param unique учитывать только уникальные посещения
     * @return список статистики просмотров
     */
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null || end.isBefore(start)) {
            log.error("Дата окнчания раньше даты начала");
            return List.of();
        }

        log.info("Запрашиваем статистику : start - %s, end - %s, uris - %s, unique - %b"
                .formatted(start.format(DATE_TIME_FORMATTER), end.format(DATE_TIME_FORMATTER), uris, unique));

        try {
            List<ViewStatsDto> views = restClient.get()
                    //.uri("/stats", uriVariables)
                    .uri(uriBuilder -> uriBuilder
                            .path("/stats")  //
                            .queryParam("start", start.format(DATE_TIME_FORMATTER))
                            .queryParam("end", end.format(DATE_TIME_FORMATTER))
                            .queryParam("unique", unique)
                            .queryParam("uris", uris != null ? String.join(",", uris) : "")
                            .build()
                    )
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                    });

            log.info("Результат - %s".formatted(views.toString()));
            return views;
        } catch (ResourceAccessException ex) {
            log.error("Сервер не доступен");
            return List.of();
        } catch (RestClientException ex) {
            log.error(ex.getMessage());
            return List.of();
        }
    }
}