package ru.practicum;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import  org.springframework.util.MultiValueMap;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class StatsClient {
    private final RestClient restClient;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DiscoveryClient discoveryClient;
    private final String statsServerId;
    private final RetryTemplate retryTemplate;

    public StatsClient(@Value("${stats-client.id}") String statsServerId,
                       DiscoveryClient discoveryClient,
                       RetryTemplate retryTemplate) {
        this.statsServerId = statsServerId;
        this.discoveryClient = discoveryClient;
        this.retryTemplate = retryTemplate;
        this.restClient = RestClient.builder().build();
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
        if (restClient == null) {
            log.error("Сервер статистики не найден");
            return false;
        }

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
                    //.uri("/hit")
                    .uri(makeUri("/hit"))
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
        if (restClient == null) {
            log.error("Сервер статистики не найден");
            return List.of();
        }

        if (start == null || end == null || end.isBefore(start)) {
            log.error("Дата окончания раньше даты начала");
            return List.of();
        }

        log.info("Запрашиваем статистику : start - %s, end - %s, uris - %s, unique - %b"
                .formatted(start.format(DATE_TIME_FORMATTER), end.format(DATE_TIME_FORMATTER), uris, unique));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("start", start.format(DATE_TIME_FORMATTER));
        params.add("end", end.format(DATE_TIME_FORMATTER));
        params.add("unique", Boolean.valueOf(unique).toString());
        params.add("uris", uris != null ? String.join(",", uris) : "");

        try {



            List<ViewStatsDto> views = restClient.get()
                    .uri(makeUri("/stats", params))
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

    private URI makeUri(String path) {
        return makeUri(path, null);
    }

    private URI makeUri(String path, MultiValueMap<String, String> params) {
        ServiceInstance instance = retryTemplate.execute( context -> getInstance());
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("http://" + instance.getHost() + ":" + instance.getPort())
                .path(path)
                .queryParams(params)
                .build();

        return uriComponents.toUri();
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient.getInstances(statsServerId).getFirst();
        } catch (Exception ex) {
            log.error("Сервер статистики не найден");
            throw new RuntimeException("Сервер статистики не найден");
        }
    }
}