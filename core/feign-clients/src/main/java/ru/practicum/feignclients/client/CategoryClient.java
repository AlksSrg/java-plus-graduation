package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.config.FeignConfiguration;

/**
 * Feign-клиент для взаимодействия с category-service.
 * Предоставляет методы для получения категорий.
 */
@FeignClient(name = "category-service", path = "/categories", configuration = FeignConfiguration.class)
public interface CategoryClient {

    /**
     * Получает категорию по её идентификатору.
     *
     * @param catId идентификатор категории
     * @return DTO категории
     */
    @GetMapping("/{catId}")
    CategoryDto getCategoryById(@PathVariable("catId") long catId);
}