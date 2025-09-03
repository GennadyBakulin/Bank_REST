package com.example.bankcards.dto.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Ответ с пагинированным списком")
public class PageDtoResponse<T> {

    @Schema(description = "Содержимое страницы")
    private List<T> content;

    @Schema(description = "Общее количество элементов", example = "100")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "10")
    private int totalPages;

    @Schema(description = "Текущий номер страницы", example = "0")
    private int number;
}
