package org.judexmars.imagecrud.exception.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.dto.BaseResponseDto;
import org.judexmars.imagecrud.exception.AccountAlreadyExistsException;
import org.judexmars.imagecrud.exception.BaseNotFoundException;
import org.judexmars.imagecrud.exception.DeleteFileException;
import org.judexmars.imagecrud.exception.InvalidJwtException;
import org.judexmars.imagecrud.service.MessageRenderer;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@RequiredArgsConstructor
public class AppExceptionHandler {

    private final MessageRenderer messageRenderer;

    @ExceptionHandler(BaseNotFoundException.class)
    public ResponseEntity<BaseResponseDto> handleResourceNotFound(BaseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponseDto(false, messageRenderer.render(ex.getMessageCode(), ex.getArgs())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponseDto> handleAccessDeniedException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new BaseResponseDto(false, messageRenderer.render("exception.access_denied")));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponseDto> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BaseResponseDto(false, messageRenderer.render("exception.bad_credentials")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<BaseResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> body =
                ex.getBindingResult().getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .filter(Objects::nonNull)
                        .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BaseResponseDto(false, body.toString()));
    }

    @ExceptionHandler(
            {InvalidJwtException.class, ExpiredJwtException.class,
                    UnsupportedJwtException.class,
                    MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<BaseResponseDto> handleAccountJwtException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BaseResponseDto(false, messageRenderer.render("exception.jwt")));
    }

    @ExceptionHandler(DeleteFileException.class)
    public ResponseEntity<BaseResponseDto> handleDeleteFile(DeleteFileException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseResponseDto(false, messageRenderer.render(ex.getMessageCode(), ex.getArgs())));
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<BaseResponseDto> handleAccountAlreadyExists(AccountAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BaseResponseDto(false, messageRenderer.render(ex.getMessageCode(), ex.getArgs())));
    }
}
