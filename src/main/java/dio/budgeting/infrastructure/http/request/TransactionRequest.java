package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Corpo do POST /transactions. O valor vai em centavos para evitar problemas com casas decimais. */
public record TransactionRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull Category category,
        @Positive long amountInCents) {

    public PersistTransactionInput toInput() {
        return new PersistTransactionInput(description, amountInCents, category);
    }
}
