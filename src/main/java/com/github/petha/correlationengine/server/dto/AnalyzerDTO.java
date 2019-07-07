package com.github.petha.correlationengine.server.dto;

import com.github.petha.correlationengine.model.Analyzer;
import com.github.petha.correlationengine.extractor.VectorExtractor;
import com.github.petha.correlationengine.model.Dictionary;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AnalyzerDTO {
    private List<ExtractorDTO> extractors;
    private String name;


    public Analyzer getAnalyzer(Dictionary dictionary) {
        List<VectorExtractor> vectorExtractors = this.getExtractors().stream()
                .map(extractor -> extractor.getKeywordExtractor(dictionary))
                .collect(Collectors.toList());

        return Analyzer.builder()
                .extractorList(vectorExtractors)
                .dictionary(dictionary)
                .name(this.getName())
                .build();
    }
}
