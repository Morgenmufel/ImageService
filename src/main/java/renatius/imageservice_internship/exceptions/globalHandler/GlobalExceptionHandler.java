package renatius.imageservice_internship.exceptions.globalHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import renatius.imageservice_internship.dto.ErrorResponseDto;
import renatius.imageservice_internship.exceptions.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommentImageNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCommentImageNotFound(CommentImageNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleImageNotFound(ImageNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LikeCommentNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleLikeCommentNotFound(LikeCommentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LikeImageNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleLikeImageNotFound(LikeImageNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SocialUserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(SocialUserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SocialUserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(SocialUserAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(S3IOException.class)
    public ResponseEntity<ErrorResponseDto> handleS3IOException(S3IOException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "S3 error: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();

        return new ResponseEntity<>(error, status);
    }
}

