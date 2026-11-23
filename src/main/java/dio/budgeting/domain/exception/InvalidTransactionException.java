package dio.budgeting.domain.exception;

/** Regra de negócio violada ao criar ou consultar uma transação. */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
