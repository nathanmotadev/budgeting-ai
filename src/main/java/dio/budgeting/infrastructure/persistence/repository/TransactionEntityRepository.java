package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionEntityRepository extends CrudRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findAllByCategoryOrderByCreatedAtDesc(Category category);

    List<TransactionEntity> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant from, Instant to);

    List<TransactionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
