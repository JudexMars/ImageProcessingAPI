package org.judexmars.imagecrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.dto.BaseResponseDto;
import org.judexmars.imagecrud.dto.image.GetImagesResponseDto;
import org.judexmars.imagecrud.dto.image.UploadImageResponseDto;
import org.judexmars.imagecrud.exception.UploadFailedException;
import org.judexmars.imagecrud.service.AccountService;
import org.judexmars.imagecrud.service.ImageService;
import org.judexmars.imagecrud.service.MessageRenderer;
import org.judexmars.imagecrud.utils.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image Controller.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Auth")
@Tag(name = "Image Controller", description = "Базовый CRUD API для работы с картинками")
public class ImageController {

  private final ImageService imageService;
  private final AccountService accountService;
  private final SecurityUtils securityUtils;
  private final MessageRenderer messageRenderer;

  /**
   * Upload image endpoint.
   *
   * @param file multipart file
   * @return {@link UploadImageResponseDto}
   * @throws UploadFailedException if upload failed
   */
  @Operation(summary = "Загрузка нового изображения в систему",
      description = """
          В рамках данного метода необходимо:
          1. Провалидировать файл. Максимальный размер файла - 10Мб,\s
          поддерживаемые расширения - png, jpeg.
          2. Загрузить файл в S3 хранилище.
          3. Сохранить в БД мета-данные файла - название; размер; ИД файла в S3; ИД пользователя,\s
          которому файл принадлежит.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Файл не прошел валидацию",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))),
      @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class)))
  })
  @PreAuthorize("hasAuthority('UPLOAD_IMAGE')")
  @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = "application/json")
  public UploadImageResponseDto uploadImage(@RequestPart(value = "file") MultipartFile file)
      throws UploadFailedException {
    return imageService.uploadImage(file, securityUtils.getLoggedInUsername());
  }

  /**
   * Download image endpoint.
   *
   * @param id image id
   * @return image in binary format
   * @throws Exception if download failed
   */
  @Operation(summary = "Скачивание файла по ИД", description = """
      В рамках данного метода необходимо:
      1. Проверить, есть ли такой файл в системе.
      2. Проверить, доступен ли данный файл пользователю.
      3. Скачать файл.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
          content = @Content(schema = @Schema(type = "string", format = "binary"))),
      @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))),
      @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class)))
  })
  @PreAuthorize("hasAuthority('DOWNLOAD_IMAGE')")
  @GetMapping(value = "/image/{image-id}")
  public ResponseEntity<byte[]> downloadImage(@PathVariable(name = "image-id") UUID id)
      throws Exception {
    var meta = imageService.getImageMeta(id);
    var image = imageService.downloadImage(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + meta.filename())
        .body(image);
  }

  /**
   * Delete image endpoint.
   *
   * @param id image id
   * @return {@link BaseResponseDto} with information about success
   */
  @Operation(summary = "Удаление файла по ИД", description = """
      В рамках данного метода необходимо:
      1. Проверить, есть ли такой файл в системе.
      2. Проверить, доступен ли данный файл пользователю.
      3. Удалить файл.""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "404", description = "Файл не найден в системе или недоступен",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
          useReturnTypeSchema = true)
  })
  @PreAuthorize("hasAuthority('DELETE_IMAGE')")
  @DeleteMapping(value = "/image/{image-id}", produces = "application/json")
  public BaseResponseDto deleteImage(@PathVariable(name = "image-id") UUID id) {
    imageService.deleteImage(id);
    return new BaseResponseDto(true, messageRenderer.render("response.image.delete_successful"));
  }

  /**
   * Get images for current user endpoint.
   *
   * @return {@link GetImagesResponseDto} containing a {@link java.util.List} of Images
   */
  @Operation(summary = "Получение списка изображений, которые доступны пользователю",
      description = """
          В рамках данного метода необходимо:
          1. Получить мета-информацию о всех изображениях, которые доступны пользователю""")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Успех выполнения операции",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))),
  })
  @GetMapping(value = "/images", produces = "application/json")
  public GetImagesResponseDto getImages() {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    var account = accountService.getByUsername(username);
    return new GetImagesResponseDto(imageService.getImagesOfUser(account.id()));
  }
}
