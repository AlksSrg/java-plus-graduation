package ru.practicum;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class StatsClient {
    private final RestClient restClient;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DiscoveryClient discoveryClient;
    private final String statsServerId;
    private final RetryTemplate retryTemplate;
    private final String baseUrl;

    // Конструктор для использования с DiscoveryClient
    public StatsClient(@Value("${stats-client.id}") String statsServerId,
                       DiscoveryClient discoveryClient,
                       RetryTemplate retryTemplate) {
        this.statsServerId = statsServerId;
        this.discoveryClient = discoveryClient;
        this.retryTemplate = retryTemplate;
        this.restClient = RestClient.builder().build();
        this.baseUrl = null;
    }

    // Конструктор для тестирования (без DiscoveryClient)
    public StatsClient(String baseUrl, RetryTemplate retryTemplate) {
        this.statsServerId = null;
        this.discoveryClient = null;
        this.retryTemplate = retryTemplate;
        this.restClient = RestClient.builder().build();
        this.baseUrl = baseUrl;
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
            URI requestUri = makeUri("/hit");
            ResponseEntity<Void> response = restClient.post()
                    .uri(requestUri)
                    .contentType(APPLICATION_JSON)
                    .body(endpointHit)
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (ResourceAccessException ex) {
            log.error("Сервер не доступен");
            return false;
        } catch (RestClientException ex) {
            log.error("Ошибка при сохранении статистики: {}", ex.getMessage());
            return false;
        } catch (RuntimeException ex) {
            log.error("Ошибка: {}", ex.getMessage());
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
            log.error("Дата окончания раньше даты начала");
            return Collections.emptyList();
        }

        log.info("Запрашиваем статистику : start - {}, end - {}, uris - {}, unique - {}",
                start.format(DATE_TIME_FORMATTER), end.format(DATE_TIME_FORMATTER), uris, unique);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("start", start.format(DATE_TIME_FORMATTER));
        params.add("end", end.format(DATE_TIME_FORMATTER));
        params.add("unique", Boolean.toString(unique));

        if (uris != null && !uris.isEmpty()) {
            params.add("uris", String.join(",", uris));
        }

        try {
            URI requestUri = makeUri("/stats", params);
            List<ViewStatsDto> views = restClient.get()
                    .uri(requestUri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                    });

            if (views != null) {
                log.info("Получено {} записей статистики", views.size());
                return views;
            } else {
                return Collections.emptyList();
            }
        } catch (ResourceAccessException ex) {
            log.error("Сервер не доступен");
            return Collections.emptyList();
        } catch (RestClientException ex) {
            log.error("Ошибка при получении статистики: {}", ex.getMessage());
            return Collections.emptyList();
        } catch (RuntimeException ex) {
            log.error("Ошибка: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private URI makeUri(String path) {
        return makeUri(path, null);
    }

    private URI makeUri(String path, MultiValueMap<String, String> params) {
        try {
            String host;
            int port;
            if (baseUrl != null && !baseUrl.isEmpty()) {
                UriComponents baseComponents = UriComponentsBuilder.fromHttpUrl(baseUrl).build();
                host = baseComponents.getHost();
                port = baseComponents.getPort() != -1 ? baseComponents.getPort() : 80;
            } else {
                ServiceInstance instance = retryTemplate.execute(context -> getInstance());
                host = instance.getHost();
                port = instance.getPort();
            }

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                    .scheme("http")
                    .host(host)
                    .port(port)
                    .path(path);

            if (params != null) {
                uriBuilder.queryParams(params);
            }

            return uriBuilder.build().toUri();
        } catch (Exception ex) {
            log.error("Ошибка при создании URI: {}", ex.getMessage());
            throw new RuntimeException("Сервис статистики не доступен");
        }
    }

    private ServiceInstance getInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(statsServerId);
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("Сервер статистики не найден");
            }
            return instances.getFirst();
        } catch (Exception ex) {
            log.error("Сервер статистики не найден: {}", ex.getMessage());
            throw new RuntimeException("Сервер статистики не найден");
        }
    }
}