package ru.practicum.mapper;

import dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Маппер для преобразования ResultSet в объект ViewStatsDto.
 */
@Slf4j
public class ViewStatsDtoRowMapper implements RowMapper<ViewStatsDto> {

    /**
     * Преобразует строку ResultSet в объект ViewStatsDto.
     *
     * @param rs     ResultSet с данными
     * @param rowNum номер строки
     * @return объект ViewStatsDto
     * @throws SQLException в случае ошибок SQL
     */
    @Override
    public ViewStatsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ViewStatsDto.builder()
                .app(rs.getString("app"))
                .uri(rs.getString("uri"))
                .hits(rs.getLong("hits"))
                .build();
    }
}