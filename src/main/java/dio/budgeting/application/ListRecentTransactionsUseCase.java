package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class ListRecentTransactionsUseCase {
    static final int DEFAULT_LIMIT = 5;
    static final int MAX_LIMIT = 50;

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public ListRecentTransactionsUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "list-recent-transactions",
            description = "Lista os últimos gastos registrados, do mais novo para o mais antigo. "
                    + "Também serve para descobrir o id de um gasto antes de apagá-lo.")
    public List<TransactionOutput> execute(
            @ToolParam(description = "Quantidade de gastos (padrão 5, máximo 50)", required = false) Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        return transactionRepository.findMostRecent(safeLimit).stream()
                .map(t -> TransactionOutput.from(t, clock.getZone()))
                .toList();
    }
}
