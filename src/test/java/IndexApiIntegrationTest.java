import com.github.petha.correlationengine.SpringBoot;
import com.github.petha.correlationengine.server.api.AnalyzeAPI;
import com.github.petha.correlationengine.server.api.IndexAPI;
import com.github.petha.correlationengine.server.dto.AnalyzerDTO;
import com.github.petha.correlationengine.server.dto.DocumentDTO;
import com.github.petha.correlationengine.server.dto.ExtractorDTO;
import com.github.petha.correlationengine.server.dto.MatchDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBoot.class)
@AutoConfigureMockMvc
public class IndexApiIntegrationTest {
    @Autowired
    IndexAPI indexAPI;

    @Autowired
    AnalyzeAPI analyzeAPI;

    @Test
    public void test() {
        AnalyzerDTO analyzerDTO = new AnalyzerDTO();
        ExtractorDTO extractorDTO = new ExtractorDTO();

        extractorDTO.setName("uniq_words");
        extractorDTO.setSourceField("description");

        analyzerDTO.setName("UniqWords");
        analyzerDTO.setExtractors(List.of(extractorDTO));
        this.analyzeAPI.createAnalyzer(analyzerDTO);

        DocumentDTO document1 = new DocumentDTO();
        document1.getFields().put("description", "this document is the first of three");
        document1.setId(UUID.randomUUID());

        DocumentDTO document2 = new DocumentDTO();
        document2.getFields().put("description", "the text of which none should match");
        document2.setId(UUID.randomUUID());

        DocumentDTO document3 = new DocumentDTO();
        document3.getFields().put("description", "three of first the is document this");
        document3.setId(UUID.randomUUID());


        this.indexAPI.createDocument(document1);
        this.indexAPI.createDocument(document2);
        this.indexAPI.createDocument(document3);
        List<MatchDTO> uniqWords = this.indexAPI.getMatching("UniqWords", document1.getId())
                .collect(Collectors.toList());
        Assert.assertTrue(uniqWords.stream().map(MatchDTO::getDocumentId).anyMatch(x -> document3.getId().equals(x)));
        Assert.assertFalse(uniqWords.stream().map(MatchDTO::getDocumentId).anyMatch(x -> document2.getId().equals(x)));
    }

}
