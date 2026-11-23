package dio.budgeting.support;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositório fake para testar os casos de uso sem banco e sem Spring. */
public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<TransactionId, Transaction> data = new LinkedHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        data.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        return newestFirst().stream().filter(t -> t.getCategory() == category).toList();
    }

    @Override
    public List<Transaction> findAllCreatedBetween(Instant from, Instant to) {
        return data.values().stream()
                .filter(t -> !t.getCreatedAt().isBefore(from) && t.getCreatedAt().isBefore(to))
                .toList();
    }

    @Override
    public List<Transaction> findMostRecent(int limit) {
        return newestFirst().stream().limit(limit).toList();
    }

    @Override
    public void delete(TransactionId id) {
        data.remove(id);
    }

    public int size() {
        return data.size();
    }

    private List<Transaction> newestFirst() {
        return data.values().stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
    }
}
