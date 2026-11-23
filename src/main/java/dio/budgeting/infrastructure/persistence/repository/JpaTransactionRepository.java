package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaTransactionRepository implements TransactionRepository {
    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionEntityRepository.save(TransactionEntity.from(transaction)).toDomain();
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return transactionEntityRepository.findById(id.uuid()).map(TransactionEntity::toDomain);
    }

    @Override
    public List<Transaction> findAllByCategory(Category category) {
        return toDomain(transactionEntityRepository.findAllByCategoryOrderByCreatedAtDesc(category));
    }

    @Override
    public List<Transaction> findAllCreatedBetween(Instant from, Instant to) {
        return toDomain(transactionEntityRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to));
    }

    @Override
    public List<Transaction> findMostRecent(int limit) {
        return toDomain(transactionEntityRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)));
    }

    @Override
    public void delete(TransactionId id) {
        transactionEntityRepository.deleteById(id.uuid());
    }

    private static List<Transaction> toDomain(List<TransactionEntity> entities) {
        return entities.stream().map(TransactionEntity::toDomain).toList();
    }
}
