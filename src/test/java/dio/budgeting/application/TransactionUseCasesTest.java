package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.exception.InvalidTransactionException;
import dio.budgeting.domain.exception.TransactionNotFoundException;
import dio.budgeting.support.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionUseCasesTest {
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    // 23/11/2026 às 15h em São Paulo
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T18:00:00Z"), SAO_PAULO);

    private InMemoryTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void persist_should_convertCentsToReais_inOutput() {
        var useCase = new PersistTransactionUseCase(repository, CLOCK);

        var output = useCase.execute(new PersistTransactionInput("farmácia", 4590, Category.PHARMA));

        // A base original devolvia 4590.0 aqui; o correto é R$ 45,90.
        assertThat(output.value()).isEqualTo(45.90);
        assertThat(output.categoryLabel()).isEqualTo("Farmácia");
        assertThat(output.createdAt().toLocalDate()).isEqualTo(LocalDate.of(2026, 11, 23));
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void persist_should_notSave_when_amountIsZero() {
        var useCase = new PersistTransactionUseCase(repository, CLOCK);

        assertThatThrownBy(() -> useCase.execute(new PersistTransactionInput("nada", 0, Category.OTHER)))
                .isInstanceOf(InvalidTransactionException.class);
        assertThat(repository.size()).isZero();
    }

    @Test
    void summary_should_defaultToCurrentMonth_andGroupByCategory() {
        save("mercado", 8000, Category.GROCERIES, "2026-11-02T13:00:00Z");
        save("feira", 2050, Category.GROCERIES, "2026-11-20T13:00:00Z");
        save("remédio", 4590, Category.PHARMA, "2026-11-10T13:00:00Z");
        save("mês passado", 99900, Category.BILLS, "2026-10-31T13:00:00Z");

        var summary = new SummarizeExpensesUseCase(repository, CLOCK).execute(null, null);

        assertThat(summary.startDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(summary.endDate()).isEqualTo(LocalDate.of(2026, 11, 23));
        assertThat(summary.count()).isEqualTo(3);
        assertThat(summary.total()).isEqualTo(146.40);
        assertThat(summary.byCategory()).first()
                .satisfies(c -> {
                    assertThat(c.category()).isEqualTo("GROCERIES");
                    assertThat(c.total()).isEqualTo(100.50);
                    assertThat(c.count()).isEqualTo(2);
                });
    }

    @Test
    void summary_should_respectLocalTimeZone_atDayBoundary() {
        // 01/11 às 01h UTC ainda é 31/10 em São Paulo -> não entra em novembro
        save("madrugada", 1000, Category.LEISURE, "2026-11-01T01:00:00Z");

        var summary = new SummarizeExpensesUseCase(repository, CLOCK).execute("2026-11-01", "2026-11-23");

        assertThat(summary.count()).isZero();
    }

    @Test
    void summary_should_reject_when_endIsBeforeStart() {
        var useCase = new SummarizeExpensesUseCase(repository, CLOCK);

        assertThatThrownBy(() -> useCase.execute("2026-11-20", "2026-11-01"))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @Test
    void recent_should_returnNewestFirst_andRespectLimit() {
        save("antigo", 100, Category.OTHER, "2026-11-01T10:00:00Z");
        save("novo", 200, Category.OTHER, "2026-11-22T10:00:00Z");
        save("meio", 300, Category.OTHER, "2026-11-10T10:00:00Z");

        var recent = new ListRecentTransactionsUseCase(repository, CLOCK).execute(2);

        assertThat(recent).extracting("description").containsExactly("novo", "meio");
    }

    @Test
    void delete_should_removeTransaction() {
        var saved = save("errado", 100, Category.OTHER, "2026-11-22T10:00:00Z");

        var deleted = new DeleteTransactionUseCase(repository, CLOCK).execute(saved.getId().uuid().toString());

        assertThat(deleted.description()).isEqualTo("errado");
        assertThat(repository.size()).isZero();
    }

    @Test
    void delete_should_fail_when_idDoesNotExist() {
        var useCase = new DeleteTransactionUseCase(repository, CLOCK);

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID().toString()))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    private Transaction save(String description, long cents, Category category, String instant) {
        return repository.save(Transaction.create(description, cents, category, Instant.parse(instant)));
    }
}
