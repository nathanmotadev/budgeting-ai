package dio.budgeting.infrastructure.ai;

/** Falha ao falar com o provedor de IA (chave inválida, limite de uso, rede etc.). */
public class AssistantUnavailableException extends RuntimeException {
    public AssistantUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
