package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.exception.InvalidTransactionException;
import dio.budgeting.domain.exception.TransactionNotFoundException;
import dio.budgeting.infrastructure.ai.AssistantUnavailableException;
import dio.budgeting.infrastructure.ai.InvalidAudioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converte erros em respostas padronizadas (RFC 9457 - Problem Details). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({InvalidTransactionException.class, InvalidAudioException.class})
    ProblemDetail badRequest(RuntimeException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    ProblemDetail notFound(TransactionNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidBody(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        var problem = problem(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ProblemDetail unreadable(Exception e) {
        return problem(HttpStatus.BAD_REQUEST,
                "Requisição inválida. Categorias aceitas: " + Arrays.toString(Category.values()));
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    ProblemDetail missingPart(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, "Envie o arquivo de áudio no campo 'file' (multipart/form-data).");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail tooLarge(MaxUploadSizeExceededException e) {
        return problem(HttpStatus.CONTENT_TOO_LARGE, "O arquivo de áudio é grande demais.");
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    ProblemDetail aiUnavailable(AssistantUnavailableException e) {
        return problem(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
