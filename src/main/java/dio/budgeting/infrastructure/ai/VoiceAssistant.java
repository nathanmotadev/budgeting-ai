package dio.budgeting.infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Orquestra o fluxo de voz completo:
 * áudio -> texto (transcrição) -> assistente com tools -> texto -> áudio (síntese).
 */
@Component
public class VoiceAssistant {
    private static final Logger log = LoggerFactory.getLogger(VoiceAssistant.class);

    private final TranscriptionModel transcriptionModel;
    private final BudgetAssistant budgetAssistant;
    private final TextToSpeechModel textToSpeechModel;

    public VoiceAssistant(TranscriptionModel transcriptionModel,
                          BudgetAssistant budgetAssistant,
                          TextToSpeechModel textToSpeechModel) {
        this.transcriptionModel = transcriptionModel;
        this.budgetAssistant = budgetAssistant;
        this.textToSpeechModel = textToSpeechModel;
    }

    public VoiceReply handle(Resource audio) {
        var transcription = transcribe(audio);
        log.info("Transcrição: {}", transcription);

        var answer = budgetAssistant.reply(transcription);
        log.info("Resposta do assistente: {}", answer);

        return new VoiceReply(transcription, answer, synthesize(answer));
    }

    private String transcribe(Resource audio) {
        String text;
        try {
            text = transcriptionModel.transcribe(audio);
        } catch (RuntimeException e) {
            throw new AssistantUnavailableException("Não foi possível transcrever o áudio agora.", e);
        }
        if (text == null || text.isBlank()) {
            throw new InvalidAudioException("Não consegui entender nada no áudio. Tente falar mais perto do microfone.");
        }
        return text.strip();
    }

    private byte[] synthesize(String text) {
        try {
            return textToSpeechModel.call(text);
        } catch (RuntimeException e) {
            throw new AssistantUnavailableException("Não foi possível gerar o áudio da resposta agora.", e);
        }
    }

    public record VoiceReply(String transcription, String answer, byte[] audio) {
    }
}
