package dio.budgeting.domain.exception;

import dio.budgeting.domain.TransactionId;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(TransactionId id) {
        super("Transação não encontrada: " + id.uuid());
    }
}
