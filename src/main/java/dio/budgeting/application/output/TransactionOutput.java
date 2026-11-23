package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @param value valor em reais (a base original devolvia os centavos como se fossem reais;
 *              aqui a conversão é feita de verdade)
 */
public record TransactionOutput(String id,
                                String description,
                                String category,
                                String categoryLabel,
                                double value,
                                LocalDateTime createdAt) {

    public static TransactionOutput from(Transaction transaction, ZoneId zone) {
        var createdAt = transaction.getCreatedAt() == null
                ? null
                : LocalDateTime.ofInstant(transaction.getCreatedAt(), zone);

        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                transaction.getCategory().label(),
                Money.toReais(transaction.getAmount()),
                createdAt);
    }
}
