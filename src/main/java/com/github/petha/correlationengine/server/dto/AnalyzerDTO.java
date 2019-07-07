package com.github.petha.correlationengine.server.dto;

import com.github.petha.correlationengine.extractor.VectorExtractor;
import com.github.petha.correlationengine.model.Analyzer;
import com.github.petha.correlationengine.model.Dictionary;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AnalyzerDTO {
    @NotNull
    @Valid
    private List<ExtractorDTO> extractors;

    @NotBlank
    @Pattern(regexp="^[a-zA-Z0-9]{1,10}$",message="length must be between 1 and 10 characters")
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
