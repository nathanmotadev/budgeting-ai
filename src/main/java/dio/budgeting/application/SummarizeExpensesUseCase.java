package dio.budgeting.application;

import dio.budgeting.application.output.ExpenseSummaryOutput;
import dio.budgeting.application.output.ExpenseSummaryOutput.CategoryTotal;
import dio.budgeting.application.output.Money;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.exception.InvalidTransactionException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SummarizeExpensesUseCase {
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public SummarizeExpensesUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "summarize-expenses",
            description = "Calcula o total gasto em um período, com o total por categoria. "
                    + "Use para perguntas como 'quanto gastei este mês?' ou 'quanto gastei em mercado na semana passada?'. "
                    + "Sem datas, considera do dia 1 do mês atual até hoje.")
    public ExpenseSummaryOutput execute(
            @ToolParam(description = "Data inicial no formato yyyy-MM-dd (inclusive)", required = false) String startDate,
            @ToolParam(description = "Data final no formato yyyy-MM-dd (inclusive)", required = false) String endDate) {

        var today = LocalDate.now(clock);
        var start = parseOrDefault(startDate, today.withDayOfMonth(1));
        var end = parseOrDefault(endDate, today);

        if (end.isBefore(start)) {
            throw new InvalidTransactionException("A data final não pode ser anterior à data inicial.");
        }

        var zone = clock.getZone();
        var from = start.atStartOfDay(zone).toInstant();
        var to = end.plusDays(1).atStartOfDay(zone).toInstant();

        return summarize(start, end, transactionRepository.findAllCreatedBetween(from, to));
    }

    private ExpenseSummaryOutput summarize(LocalDate start, LocalDate end, List<Transaction> transactions) {
        Map<Category, long[]> totals = new EnumMap<>(Category.class); // [centavos, quantidade]
        long totalCents = 0;

        for (var transaction : transactions) {
            var bucket = totals.computeIfAbsent(transaction.getCategory(), c -> new long[2]);
            bucket[0] += transaction.getAmount();
            bucket[1]++;
            totalCents += transaction.getAmount();
        }

        var byCategory = totals.entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<Category, long[]> e) -> e.getValue()[0]).reversed())
                .map(e -> new CategoryTotal(
                        e.getKey().name(),
                        e.getKey().label(),
                        Money.toReais(e.getValue()[0]),
                        (int) e.getValue()[1]))
                .toList();

        return new ExpenseSummaryOutput(start, end, Money.toReais(totalCents), transactions.size(), byCategory);
    }

    private static LocalDate parseOrDefault(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value.strip());
        } catch (DateTimeParseException e) {
            throw new InvalidTransactionException("Data inválida: " + value + " (use yyyy-MM-dd)");
        }
    }
}
