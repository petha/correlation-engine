package Server.DTO;

import Correlation.Extractor.UniqWordsExtractor;
import Correlation.Extractor.VectorExtractor;
import lombok.Data;

import java.io.IOException;
import java.util.Map;

@Data
public class ExtractorDTO {
    private String name;
    private String sourceField;
    private Map<String, String> properties;


    public VectorExtractor getKeywordExtractor() {
        if ("uniq_words".equals(this.getName())) {
            return new UniqWordsExtractor(this.getSourceField());
        }
        throw new RuntimeException("No matching extractor");
    }

}
