package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class PersistTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public PersistTransactionUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "persist-transaction",
            description = "Registra um novo gasto. Use quando a pessoa disser que gastou, pagou ou comprou algo.")
    public TransactionOutput execute(PersistTransactionInput input) {
        var transaction = Transaction.create(input.description(), input.amount(), input.category(), clock.instant());
        var saved = transactionRepository.save(transaction);
        return TransactionOutput.from(saved, clock.getZone());
    }
}
