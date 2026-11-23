package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionOutput;

import java.time.LocalDateTime;

/** @param amount valor em reais */
public record TransactionResponse(String id,
                                  String description,
                                  String category,
                                  String categoryLabel,
                                  double amount,
                                  LocalDateTime createdAt) {

    public static TransactionResponse from(TransactionOutput output) {
        return new TransactionResponse(
                output.id(),
                output.description(),
                output.category(),
                output.categoryLabel(),
                output.value(),
                output.createdAt());
    }
}
