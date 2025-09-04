package com.example.bankcards.dto.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "DTO для возврата информации об ошибке")
public class ErrorDtoResponse {

    @Schema(description = "Код ошибки", example = "400")
    private Integer code;

    @Schema(description = "Сообщение об ошибке", example = "Неверный формат запроса")
    private String message;

    @Schema(description = "Время возникновения ошибки", example = "2023-12-01T10:15:30")
    private LocalDateTime timestamp;
}
