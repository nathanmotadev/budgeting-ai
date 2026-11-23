package dio.budgeting.domain;

import dio.budgeting.domain.exception.InvalidTransactionException;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID uuid) {
    public TransactionId {
        Objects.requireNonNull(uuid, "uuid");
    }

    public TransactionId() {
        this(UUID.randomUUID());
    }

    public static TransactionId of(String value) {
        try {
            return new TransactionId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidTransactionException("Id de transação inválido: " + value);
        }
    }
}
