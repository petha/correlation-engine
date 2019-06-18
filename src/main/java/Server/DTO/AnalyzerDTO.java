package Server.DTO;

import Correlation.Model.Analyzer;
import Correlation.Extractor.VectorExtractor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AnalyzerDTO {
    private List<ExtractorDTO> extractors;
    private String name;


    public Analyzer getAnalyzer() {
        List<VectorExtractor> vectorExtractors = this.getExtractors().stream()
                .map(ExtractorDTO::getKeywordExtractor)
                .collect(Collectors.toList());

        return Analyzer.builder()
                .extractorList(vectorExtractors)
                .name(this.getName())
                .build();
    }
}
