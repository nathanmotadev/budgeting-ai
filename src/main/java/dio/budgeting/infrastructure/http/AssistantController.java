package dio.budgeting.infrastructure.http;

import dio.budgeting.infrastructure.ai.AudioUploadValidator;
import dio.budgeting.infrastructure.ai.BudgetAssistant;
import dio.budgeting.infrastructure.ai.VoiceAssistant;
import dio.budgeting.infrastructure.http.request.AssistantTextRequest;
import dio.budgeting.infrastructure.http.response.AssistantTextResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Endpoints do assistente: por voz (áudio entra, áudio sai) e por texto (para testar sem gravar). */
@RestController
@RequestMapping("/transactions/ai")
public class AssistantController {
    static final String TRANSCRIPTION_HEADER = "X-Transcription";
    static final String REPLY_HEADER = "X-Assistant-Reply";

    private final VoiceAssistant voiceAssistant;
    private final BudgetAssistant budgetAssistant;
    private final AudioUploadValidator audioValidator;

    public AssistantController(VoiceAssistant voiceAssistant,
                               BudgetAssistant budgetAssistant,
                               AudioUploadValidator audioValidator) {
        this.voiceAssistant = voiceAssistant;
        this.budgetAssistant = budgetAssistant;
        this.audioValidator = audioValidator;
    }

    /**
     * Recebe um áudio, executa o comando e devolve a resposta falada em MP3.
     * A transcrição e a resposta em texto vão nos headers (URL-encoded) para facilitar o debug.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<Resource> voice(@RequestParam("file") MultipartFile file) {
        audioValidator.validate(file);

        var reply = voiceAssistant.handle(file.getResource());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("resposta.mp3").build().toString())
                .header(TRANSCRIPTION_HEADER, encode(reply.transcription()))
                .header(REPLY_HEADER, encode(reply.answer()))
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, TRANSCRIPTION_HEADER + ", " + REPLY_HEADER)
                .body(new ByteArrayResource(reply.audio()));
    }

    /** Mesmo assistente, mas com texto. Útil para testar o Tool Calling sem gastar transcrição/síntese. */
    @PostMapping(path = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AssistantTextResponse text(@Valid @RequestBody AssistantTextRequest request) {
        return new AssistantTextResponse(request.message(), budgetAssistant.reply(request.message()));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
