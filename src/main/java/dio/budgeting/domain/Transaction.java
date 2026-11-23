package dio.budgeting.domain;

import dio.budgeting.domain.exception.InvalidTransactionException;

import java.time.Instant;

/**
 * Uma transação de gasto. O valor fica sempre em centavos (long) para evitar
 * erros de arredondamento com double.
 */
public class Transaction {
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final TransactionId id;
    private final String description;
    private final long amount;
    private final Category category;
    private final Instant createdAt;

    /** Reconstitui uma transação já existente (ex.: vinda do banco). */
    public Transaction(TransactionId id, String description, long amount, Category category, Instant createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.createdAt = createdAt;
    }

    /** Cria uma transação nova, validando as regras de negócio. */
    public static Transaction create(String description, long amount, Category category, Instant createdAt) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("A descrição do gasto é obrigatória.");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidTransactionException("A descrição pode ter no máximo " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("O valor do gasto precisa ser maior que zero.");
        }
        if (category == null) {
            throw new InvalidTransactionException("A categoria do gasto é obrigatória.");
        }
        return new Transaction(new TransactionId(), description.strip(), amount, category, createdAt);
    }

    public TransactionId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
