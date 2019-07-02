import com.github.petha.correlationengine.server.SpringBoot;
import com.github.petha.correlationengine.server.api.AnalyzeAPI;
import com.github.petha.correlationengine.server.dto.AnalyzerDTO;
import com.github.petha.correlationengine.server.dto.ExtractorDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBoot.class)
@AutoConfigureMockMvc
public class AnalyzeApiIntegrationTest {
    public static final String TEST = "Test";

    @Autowired
    AnalyzeAPI analyzeAPI;

    @Test
    public void checkAnalyzers() {
        AnalyzerDTO analyzerDTO = new AnalyzerDTO();
        ExtractorDTO extractorDTO = new ExtractorDTO();

        extractorDTO.setName("uniq_words");
        extractorDTO.setSourceField(TEST);

        analyzerDTO.setName(TEST);
        analyzerDTO.setExtractors(List.of(extractorDTO));
        this.analyzeAPI.createAnalyzer(analyzerDTO);
        List<String> strings = this.analyzeAPI.listAnalyzers();

        Assert.assertThat(strings.size(), greaterThan(0));
    }
}
