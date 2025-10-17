package pl.bpiatek.linkshortenerapigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
        log.warn("Unhandled exception caught in API Gateway: {}", ex.getMessage(), ex);

        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found"
        );

        if (ex.getMessage() != null &&
                (ex.getMessage().contains("No matching route found") ||
                        ex.getMessage().contains("status=999") ||
                        ex.getMessage().contains("Failed to handle request"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        Map<String, Object> errorBody = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}