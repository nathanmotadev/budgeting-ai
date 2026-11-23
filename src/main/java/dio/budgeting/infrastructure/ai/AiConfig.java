package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.DeleteTransactionUseCase;
import dio.budgeting.application.ListRecentTransactionsUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeExpensesUseCase;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os casos de uso como ferramentas do modelo. A IA não acessa o banco
 * diretamente: ela só pode fazer o que os casos de uso permitem, com as mesmas
 * regras de negócio usadas pela API REST.
 */
@Configuration
public class AiConfig {

    @Bean
    ChatClient budgetChatClient(ChatClient.Builder builder,
                                PersistTransactionUseCase persistTransaction,
                                ListTransactionsByCategoryUseCase listByCategory,
                                ListRecentTransactionsUseCase listRecent,
                                SummarizeExpensesUseCase summarizeExpenses,
                                DeleteTransactionUseCase deleteTransaction) {
        return builder
                .defaultTools(persistTransaction, listByCategory, listRecent, summarizeExpenses, deleteTransaction)
                .build();
    }
}
