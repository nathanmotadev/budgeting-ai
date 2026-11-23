package dio.budgeting.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * Valida o arquivo antes de qualquer chamada paga à OpenAI.
 * Formatos aceitos pela API de transcrição: mp3, mp4, mpeg, mpga, m4a, wav, webm (+ ogg e flac).
 */
@Component
public class AudioUploadValidator {
    static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm", "ogg", "flac");

    private final long maxBytes;

    public AudioUploadValidator(@Value("${budgeting.audio.max-size:25MB}") DataSize maxSize) {
        this.maxBytes = maxSize.toBytes();
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAudioException("Envie um arquivo de áudio no campo 'file'.");
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidAudioException("O áudio é maior que o limite de " + DataSize.ofBytes(maxBytes).toMegabytes() + " MB.");
        }

        var extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidAudioException("Formato de áudio não suportado: '" + extension
                    + "'. Use um destes: " + String.join(", ", ALLOWED_EXTENSIONS.stream().sorted().toList()));
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
