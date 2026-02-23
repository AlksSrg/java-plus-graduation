package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.Collections;
import java.util.List;

/**
 * Маппер для преобразования между сущностями и DTO подборок событий.
 */
@UtilityClass
public class CompilationMapper {

    /**
     * Преобразует сущность в DTO.
     *
     * @param compilation сущность подборки
     * @param events      список кратких DTO событий, входящих в подборку
     * @return DTO подборки
     */
    public static CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {
        if (compilation == null) {
            return null;
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events != null ? events : Collections.emptyList())
                .build();
    }

    /**
     * Преобразует DTO в сущность.
     *
     * @param newCompilationDto DTO для создания
     * @return сущность подборки
     */
    public static Compilation toEntity(NewCompilationDto newCompilationDto) {
        if (newCompilationDto == null) {
            return null;
        }

        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false)
                .build();
    }
}