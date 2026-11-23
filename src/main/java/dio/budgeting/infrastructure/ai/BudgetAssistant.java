package dio.budgeting.infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;

/** Recebe um pedido em texto e devolve a resposta do assistente (com Tool Calling). */
@Component
public class BudgetAssistant {
    private static final Logger log = LoggerFactory.getLogger(BudgetAssistant.class);

    private final ChatClient chatClient;
    private final String systemTemplate;
    private final Clock clock;

    public BudgetAssistant(ChatClient budgetChatClient,
                           @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                           Clock clock) {
        this.chatClient = budgetChatClient;
        this.systemTemplate = read(systemPrompt);
        this.clock = clock;
    }

    public String reply(String userMessage) {
        // A data entra no prompt a cada chamada para o modelo entender "hoje", "ontem", "este mês".
        var system = systemTemplate.replace("{today}", LocalDate.now(clock).toString());

        try {
            var answer = chatClient.prompt()
                    .system(system)
                    .user(userMessage)
                    .call()
                    .content();

            if (answer == null || answer.isBlank()) {
                throw new AssistantUnavailableException("O modelo não retornou resposta.", null);
            }
            return answer.strip();
        } catch (AssistantUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Falha ao consultar o modelo de linguagem", e);
            throw new AssistantUnavailableException("Não foi possível falar com o modelo de IA agora.", e);
        }
    }

    private static String read(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o prompt de sistema", e);
        }
    }
}
