package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

/**
 * Маппер для преобразования между сущностями и DTO подборок событий.
 * Использует MapStruct для генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface CompilationMapper {

    /**
     * Преобразует NewCompilationDto в сущность Compilation.
     * ID игнорируется, eventIds будет заполнен отдельно.
     * Для поля pinned устанавливается значение по умолчанию false.
     *
     * @param dto DTO для создания
     * @return сущность подборки
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    @Mapping(target = "pinned", defaultValue = "false")
    Compilation toEntity(NewCompilationDto dto);

    /**
     * Преобразует сущность Compilation в CompilationDto.
     *
     * @param compilation сущность подборки
     * @param events      список кратких DTO событий, входящих в подборку
     * @return DTO подборки
     */
    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation, List<EventShortDto> events);

    /**
     * Обновляет существующую сущность из UpdateCompilationRequest.
     * Игнорирует null-поля.
     *
     * @param request     DTO с обновляемыми данными
     * @param compilation обновляемая сущность
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", ignore = true)
    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateCompilationRequest request, @MappingTarget Compilation compilation);
}