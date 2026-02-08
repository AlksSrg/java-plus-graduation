package ru.practicum.repository;

import dto.EndpointHitDto;
import dto.ViewStatsDto;
import dto.ViewStatsRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.exception.UnableAddElementException;
import ru.practicum.mapper.ViewStatsDtoRowMapper;

import java.util.List;

/**
 * Реализация репозитория для работы с данными о запросах к эндпоинтам.
 */
@Slf4j
@Repository
public class EndpointHitRepositoryImpl implements EndpointHitRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public EndpointHitRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Сохраняет информацию о запросе к эндпоинту.
     *
     * @param endpointHit данные о запросе
     */
    @Override
    public void addEndpointHit(EndpointHitDto endpointHit) {
        String errorMessage = "Неудачная попытка сохранения объекта в базу данных.";

        String addEndpointHitSql = ""
                + "INSERT INTO stats (app, uri, ip, timestamp) "
                + "VALUES (:app, :uri, :ip, :timeStamp)";

        MapSqlParameterSource sqlParams = new MapSqlParameterSource();
        sqlParams.addValue("app", endpointHit.getApp());
        sqlParams.addValue("uri", endpointHit.getUri());
        sqlParams.addValue("ip", endpointHit.getIp());
        sqlParams.addValue("timeStamp", endpointHit.getTimestamp());

        int affectedRows = namedParameterJdbcTemplate.update(addEndpointHitSql, sqlParams);

        if (affectedRows == 0) {
            throw new UnableAddElementException(errorMessage, endpointHit);
        }
    }

    /**
     * Получает статистику просмотров по заданным параметрам.
     *
     * @param viewStatsRequestDto параметры запроса статистики
     * @return список статистики просмотров
     */
    @Override
    public List<ViewStatsDto> getViewStats(ViewStatsRequestDto viewStatsRequestDto) {
        if (viewStatsRequestDto.getEnd().isBefore(viewStatsRequestDto.getStart()))
            throw new UnableAddElementException("Не корректные временные интервалы",
                    viewStatsRequestDto.getEnd());

        MapSqlParameterSource sqlParams = new MapSqlParameterSource();
        sqlParams.addValue("start", viewStatsRequestDto.getStart());
        sqlParams.addValue("end", viewStatsRequestDto.getEnd());

        StringBuilder getViewStatsSql = new StringBuilder();

        if (viewStatsRequestDto.getUnique()) {
            getViewStatsSql.append("SELECT s.app, s.uri, COUNT(DISTINCT s.ip) AS hits ");
        } else {
            getViewStatsSql.append("SELECT s.app, s.uri, COUNT(s.id) hits ");
        }

        getViewStatsSql.append("FROM stats s ");
        getViewStatsSql.append("WHERE s.timestamp BETWEEN :start AND :end ");

        if (viewStatsRequestDto.getUris() != null && !viewStatsRequestDto.getUris().isEmpty()) {
            getViewStatsSql.append("AND s.uri IN (:uris) ");
            sqlParams.addValue("uris", viewStatsRequestDto.getUris());
        }

        getViewStatsSql.append("GROUP BY s.app, s.uri ");
        getViewStatsSql.append("ORDER BY hits DESC ");

        String finalSql = getViewStatsSql.toString();

        List<ViewStatsDto> viewStatsDtos = namedParameterJdbcTemplate.query(finalSql, sqlParams, new ViewStatsDtoRowMapper());

        return namedParameterJdbcTemplate.query(finalSql, sqlParams, new ViewStatsDtoRowMapper());
    }
}