package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(
        @ToolParam(description = "Descrição curta do gasto, ex.: 'compras no mercado', 'remédio para gripe'")
        String description,
        @ToolParam(description = "Valor do gasto em CENTAVOS. Ex.: R$ 80 = 8000; R$ 12,50 = 1250")
        long amount,
        @ToolParam(description = "Categoria que melhor descreve o gasto")
        Category category) {
}
