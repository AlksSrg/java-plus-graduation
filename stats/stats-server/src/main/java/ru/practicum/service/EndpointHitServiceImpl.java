package ru.practicum.service;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import dto.ViewStatsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.repository.EndpointHitRepository;

import java.util.List;

/**
 * Реализация сервиса для работы со статистикой запросов к эндпоинтам.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {
    private final EndpointHitRepository endpointHitRepository;

    public EndpointHitServiceImpl(EndpointHitRepository endpointHitRepository) {
        this.endpointHitRepository = endpointHitRepository;
    }

    /**
     * Сохраняет информацию о запросе к эндпоинту.
     *
     * @param endpointHit данные о запросе
     */
    @Override
    @Transactional
    public void addEndpointHit(EndpointHitDto endpointHit) {
        endpointHitRepository.addEndpointHit(endpointHit);
    }

    /**
     * Получает статистику просмотров по заданным параметрам.
     *
     * @param viewStatsRequestDto параметры запроса статистики
     * @return список статистики просмотров
     */
    @Override
    public List<ViewStatsDto> getViewStats(ViewStatsRequestDto viewStatsRequestDto) {
        return endpointHitRepository.getViewStats(viewStatsRequestDto);
    }
}