package dio.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AudioUploadValidatorTest {
    private final AudioUploadValidator validator = new AudioUploadValidator(DataSize.ofKilobytes(1));

    @Test
    void should_accept_m4aFile() {
        var file = new MockMultipartFile("file", "gasto.m4a", "audio/mp4", new byte[100]);
        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    void should_reject_emptyFile() {
        var file = new MockMultipartFile("file", "gasto.m4a", "audio/mp4", new byte[0]);
        assertThatThrownBy(() -> validator.validate(file)).isInstanceOf(InvalidAudioException.class);
    }

    @Test
    void should_reject_unsupportedExtension() {
        var file = new MockMultipartFile("file", "planilha.xlsx", "application/octet-stream", new byte[100]);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessageContaining("xlsx");
    }

    @Test
    void should_reject_fileAboveLimit() {
        var file = new MockMultipartFile("file", "longo.mp3", "audio/mpeg", new byte[2048]);
        assertThatThrownBy(() -> validator.validate(file)).isInstanceOf(InvalidAudioException.class);
    }
}
