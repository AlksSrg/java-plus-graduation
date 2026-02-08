package ru.practicum.repository;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import dto.ViewStatsRequestDto;

import java.util.List;

/**
 * Репозиторий для работы с данными о запросах к эндпоинтам.
 */
public interface EndpointHitRepository {

    /**
     * Сохраняет информацию о запросе к эндпоинту.
     *
     * @param endpointHit данные о запросе
     */
    void addEndpointHit(EndpointHitDto endpointHit);

    /**
     * Получает статистику просмотров по заданным параметрам.
     *
     * @param viewStatsRequestDto параметры запроса статистики
     * @return список статистики просмотров
     */
    List<ViewStatsDto> getViewStats(ViewStatsRequestDto viewStatsRequestDto);

}