package dio.budgeting.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantTextRequest(@NotBlank @Size(max = 1000) String message) {
}
