package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.category.dto.CategoryDto;

/**
 * Feign-клиент для взаимодействия с Category Service.
 */
@FeignClient(name = "category-service", path = "/categories")
public interface CategoryClient {

    /**
     * Получает категорию по идентификатору.
     *
     * @param catId идентификатор категории
     * @return DTO категории
     */
    @GetMapping("/{catId}")
    CategoryDto getCategoryById(@PathVariable("catId") long catId);
}