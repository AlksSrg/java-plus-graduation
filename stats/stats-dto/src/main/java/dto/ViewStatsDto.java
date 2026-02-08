package dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO для представления статистики просмотров.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViewStatsDto {
    String app;
    String uri;
    Long hits;
}