package dio.budgeting.domain;

import dio.budgeting.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {
    private static final Instant NOW = Instant.parse("2026-11-23T15:00:00Z");

    @Test
    void should_createTransaction_when_dataIsValid() {
        var transaction = Transaction.create("  mercado  ", 8000, Category.GROCERIES, NOW);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("mercado");
        assertThat(transaction.getAmount()).isEqualTo(8000);
        assertThat(transaction.getCreatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -100})
    void should_reject_when_amountIsNotPositive(long amount) {
        assertThatThrownBy(() -> Transaction.create("mercado", amount, Category.GROCERIES, NOW))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void should_reject_when_descriptionIsBlank() {
        assertThatThrownBy(() -> Transaction.create(" ", 100, Category.GROCERIES, NOW))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @Test
    void should_reject_when_categoryIsMissing() {
        assertThatThrownBy(() -> Transaction.create("mercado", 100, null, NOW))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @Test
    void should_reject_when_idIsNotUuid() {
        assertThatThrownBy(() -> TransactionId.of("abc"))
                .isInstanceOf(InvalidTransactionException.class);
    }
}
