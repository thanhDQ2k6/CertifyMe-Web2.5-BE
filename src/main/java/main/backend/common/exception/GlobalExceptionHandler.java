package main.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import main.backend.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<?>> handleBadCredentials(
    BadCredentialsException ex
  ) {
    log.error("Bad credentials: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
      ApiResponse.error("Invalid credentials")
    );
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<?>> handleAccessDenied(
    AccessDeniedException ex
  ) {
    log.error("Access denied: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
      ApiResponse.error("Access denied")
    );
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<?>> handleRuntimeException(
    RuntimeException ex
  ) {
    log.error("Runtime exception: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      ApiResponse.error(ex.getMessage())
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      ApiResponse.error("An unexpected error occurred")
    );
  }
}
