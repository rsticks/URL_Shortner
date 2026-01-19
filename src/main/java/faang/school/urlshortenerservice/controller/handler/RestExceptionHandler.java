package faang.school.urlshortenerservice.controller.handler;

import faang.school.urlshortenerservice.dto.ErrorResponse;
import faang.school.urlshortenerservice.exception.AuthenticationRequiredException;
import faang.school.urlshortenerservice.exception.HashNotExistException;
import faang.school.urlshortenerservice.exception.SubscriptionRequiredException;
import faang.school.urlshortenerservice.exception.UserAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildErrorResponse(ErrorCode.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(HashNotExistException.class)
    @ResponseStatus(HttpStatus.TOO_EARLY)
    public ErrorResponse handleValidationException(HashNotExistException ex) {
        log.error("HashNotExistException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.TOO_EARLY, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleValidationException(EntityNotFoundException ex) {
        log.error("EntityNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationException(UserAlreadyExistsException ex) {
        log.error("UserAlreadyExistsException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleValidationException(AuthenticationRequiredException ex) {
        log.error("AuthenticationRequiredException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(SubscriptionRequiredException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ErrorResponse handleValidationException(SubscriptionRequiredException ex) {
        log.error("SubscriptionRequiredException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleValidationException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
    }


    private ErrorResponse buildErrorResponse(ErrorCode errorCode, String errorMessage) {
        return new ErrorResponse(LocalDateTime.now(),
                errorCode.getCode().value(),
                errorCode.getMessage(), errorMessage);
    }
}
