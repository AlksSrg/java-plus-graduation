package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.category.dto.CategoryDto;

import java.util.List;

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

    /**
     * Получает список категорий по идентификаторам.
     *
     * @param ids список идентификаторов категорий
     * @return список DTO категорий
     */
    @GetMapping("/by-ids")
    List<CategoryDto> getCategoriesByIds(@RequestParam("ids") List<Long> ids);
}