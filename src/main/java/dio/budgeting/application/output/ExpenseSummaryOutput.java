package dio.budgeting.application.output;

import java.time.LocalDate;
import java.util.List;

/**
 * Resumo de gastos de um período. Os totais são calculados em Java,
 * não pelo modelo de linguagem — a IA só lê e explica o resultado.
 */
public record ExpenseSummaryOutput(LocalDate startDate,
                                   LocalDate endDate,
                                   double total,
                                   int count,
                                   List<CategoryTotal> byCategory) {

    public record CategoryTotal(String category, String categoryLabel, double total, int count) {
    }
}
