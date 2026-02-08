package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import constant.DateTimeConstants;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для запроса статистики просмотров.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsRequestDto {

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT_PATTERN)
    private LocalDateTime start;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT_PATTERN)
    private LocalDateTime end;

    private List<String> uris;

    @Builder.Default
    private Boolean unique = false;
}