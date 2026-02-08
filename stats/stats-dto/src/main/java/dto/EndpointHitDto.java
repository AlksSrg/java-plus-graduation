package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import constant.DateTimeConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO для представления информации о hit'е.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointHitDto {

    @NotBlank(message = "Поле app не может быть пустым")
    @Size(min = 1, max = 32, message = "Поле app должно быть от 1 до 32 символов")
    String app;

    @NotBlank(message = "Поле uri не может быть пустым")
    @Size(min = 1, max = 128, message = "Поле uri должно быть от 1 до 128 символов")
    String uri;

    @NotBlank(message = "Поле ip не может быть пустым")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
            message = "Некорректный формат IP-адреса")
    @Size(min = 7, max = 45, message = "Поле ip должно быть от 7 до 45 символов")
    String ip;

    @NotNull(message = "Поле timestamp не может быть пустым")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstants.DATE_TIME_FORMAT_PATTERN)
    LocalDateTime timestamp;
}