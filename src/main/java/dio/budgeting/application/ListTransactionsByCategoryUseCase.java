package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class ListTransactionsByCategoryUseCase {
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public ListTransactionsByCategoryUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "list-transactions-by-category",
            description = "Lista os gastos de uma categoria específica (ex.: tudo que foi gasto em farmácia)")
    public List<TransactionOutput> execute(@ToolParam(description = "Categoria dos gastos") Category category) {
        return transactionRepository.findAllByCategory(category).stream()
                .map(t -> TransactionOutput.from(t, clock.getZone()))
                .toList();
    }
}
