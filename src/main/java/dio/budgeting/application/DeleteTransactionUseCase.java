package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.exception.TransactionNotFoundException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class DeleteTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "delete-transaction",
            description = "Apaga um gasto pelo id. Só use quando a pessoa pedir explicitamente para apagar ou "
                    + "corrigir um lançamento. Se não souber o id, consulte antes com list-recent-transactions.")
    public TransactionOutput execute(@ToolParam(description = "Id (UUID) do gasto a ser apagado") String id) {
        var transactionId = TransactionId.of(id);
        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        transactionRepository.delete(transactionId);
        return TransactionOutput.from(transaction, clock.getZone());
    }
}
