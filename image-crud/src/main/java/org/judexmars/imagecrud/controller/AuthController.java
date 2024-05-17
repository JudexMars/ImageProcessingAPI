package org.judexmars.imagecrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.BaseResponseDto;
import org.judexmars.imagecrud.dto.account.CreateAccountDto;
import org.judexmars.imagecrud.dto.auth.JwtRefreshRequestDto;
import org.judexmars.imagecrud.dto.auth.JwtRequestDto;
import org.judexmars.imagecrud.dto.auth.JwtResponseDto;
import org.judexmars.imagecrud.service.AccountService;
import org.judexmars.imagecrud.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller.
 */
@RestController
@Slf4j
@RequestMapping("/auth")
@Tag(name = "Authentication Controller",
    description = "API для аутентификации, регистрации и обновления токенов")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AccountService accountService;

  /**
   * Login endpoint.
   *
   * @param requestDto request body
   * @return {@link JwtResponseDto}
   */
  @Operation(summary = "Вход в аккаунт")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Вход успешен", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Некорректный формат данных", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))
      }),
      @ApiResponse(responseCode = "401", description = "Некорректные реквизиты", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))
      })
  })
  @PostMapping(value = "/login", produces = "application/json")
  public JwtResponseDto login(@RequestBody @Valid JwtRequestDto requestDto) {
    return authService.authenticate(requestDto.username(), requestDto.password());
  }

  /**
   * Signup endpoint.
   *
   * @param requestDto request body
   * @return {@link JwtResponseDto}
   */
  @Operation(summary = "Регистрация аккаунта")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Регистрация успешна",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Некорректный формат данных", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))
      }),
      @ApiResponse(responseCode = "401", description = "Некорректные реквизиты", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))
      })
  })
  @PostMapping(value = "/signup", produces = "application/json")
  public JwtResponseDto signUp(@RequestBody @Valid CreateAccountDto requestDto) {
    var createdAccountDto = accountService.createAccount(requestDto);
    return authService.authenticate(createdAccountDto.username(), requestDto.password());
  }

  /**
   * Refresh token endpoint.
   *
   * @param requestDto request body
   * @return {@link JwtResponseDto}
   */
  @Operation(summary = "Обновление токена")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Создан новый токен",
          useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Некорректный формат данных", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))
      }),
      @ApiResponse(responseCode = "403", description = "Некорректные реквизиты", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = BaseResponseDto.class))
      })
  })
  @PostMapping(value = "/refresh", produces = "application/json")
  public JwtResponseDto refresh(@RequestBody @Valid JwtRefreshRequestDto requestDto) {
    return authService.refresh(requestDto.token());
  }
}
