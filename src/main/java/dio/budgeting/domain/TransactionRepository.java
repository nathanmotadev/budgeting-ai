package dio.budgeting.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    List<Transaction> findAllByCategory(Category category);

    /** Transações criadas no intervalo [from, to). */
    List<Transaction> findAllCreatedBetween(Instant from, Instant to);

    /** As transações mais recentes, da mais nova para a mais antiga. */
    List<Transaction> findMostRecent(int limit);

    void delete(TransactionId id);
}
