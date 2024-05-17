package org.judexmars.imagecrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.dto.BaseResponseDto;
import org.judexmars.imagecrud.dto.imagefilters.ApplyImageFiltersResponse;
import org.judexmars.imagecrud.dto.imagefilters.FilterType;
import org.judexmars.imagecrud.dto.imagefilters.GetModifiedImageDto;
import org.judexmars.imagecrud.service.AccountService;
import org.judexmars.imagecrud.service.ImageFiltersService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller which handles applying filters to images.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Auth")
@Tag(name = "Image Filters Controller",
    description = "API для применения фильтров к загруженным картинкам")
public class ImageFiltersController {

  private final ImageFiltersService imageFiltersService;

  private final AccountService accountService;

  /**
   * Apply selected filters to the uploaded image.
   *
   * @return dto containing id of user's request
   */
  @Operation(summary = "Применение указанных фильтров к изображению", description = """
      В рамках данного метода необходимо:
      1. Проверить, есть ли у пользователя доступ к файлу
      2. Сохранить в БД новый запрос на изменение файла:
          1. статус = WIP
          2. ИД оригинальной картинки = ИД оригинального файла
          3. ИД измененной картинки = null
          4. ИД запроса = уникальный ИД запроса в системе
      3. Отправить в Kafka событие о создании запроса
      4. Убедиться, что шаг 3 выполнен успешно, в противном случае выполнить повторную попытку
      5. Вернуть пользователю ИД его запроса""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))),
      @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class)))
  })
  @PostMapping(value = "/image/{image-id}/filters/apply", produces = "application/json")
  public ApplyImageFiltersResponse applyFilters(@PathVariable(name = "image-id") UUID imageId,
                                                   @RequestParam List<FilterType> filters,
                                                   @RequestBody Map<String, String> props,
                                                   @AuthenticationPrincipal String username) {
    var account = accountService.getByUsername(username);
    return imageFiltersService.applyFilters(imageId, filters, props, account.id());
  }

  /**
   * Get modified image by request id.
   *
   * @return id of the modified image
   */
  @Operation(summary = "Получение ИД измененного файла по ИД запроса", description = """
      В рамках данного метода необходимо найти и вернуть по ИД пользовательского запроса\s
      ИД соответствующего ему файла и статус, в котором находится процесс применения фильтров.
      По ИД оригинального изображения нужно убедиться, что ИД запроса относится к нему и\s
      что у пользователя есть доступ к данному изображению (оригинальному).
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))),
      @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class)))
  })
  @GetMapping(value = "/image/{image-id}/filters/{request-id}", produces = "application/json")
  public GetModifiedImageDto getModifiedImage(@PathVariable(name = "image-id") UUID imageId,
                                              @PathVariable(name = "request-id") UUID requestId,
                                              @AuthenticationPrincipal String username) {
    var account = accountService.getByUsername(username);
    return imageFiltersService.getApplyImageFilterRequest(imageId, requestId, account.id());
  }
}
