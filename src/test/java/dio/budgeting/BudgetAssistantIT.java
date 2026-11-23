package dio.budgeting;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.ai.BudgetAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Teste ponta a ponta do Tool Calling com o modelo real (precisa de OPENAI_API_KEY). */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class BudgetAssistantIT {
    @Autowired
    BudgetAssistant assistant;

    @Autowired
    TransactionRepository repository;

    @Test
    void should_persistTransactionInCents_when_userDescribesExpense() {
        var reply = assistant.reply("Gastei 45 reais e 90 centavos na farmácia comprando remédio");
        System.out.println(reply);

        var pharma = repository.findAllByCategory(Category.PHARMA);
        assertThat(pharma).anySatisfy(t -> assertThat(t.getAmount()).isEqualTo(4590));
    }

    @Test
    void should_answerTotal_usingSummaryTool() {
        assistant.reply("Gastei 80 reais no mercado");

        var reply = assistant.reply("Quanto eu gastei com mercado este mês?");
        System.out.println(reply);

        assertThat(reply).contains("80");
    }
}
