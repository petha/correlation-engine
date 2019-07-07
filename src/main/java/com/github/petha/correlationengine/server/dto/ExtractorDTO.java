package com.github.petha.correlationengine.server.dto;

import com.github.petha.correlationengine.exceptions.ApplicationException;
import com.github.petha.correlationengine.extractor.UniqWordsExtractor;
import com.github.petha.correlationengine.extractor.VectorExtractor;
import com.github.petha.correlationengine.model.Dictionary;
import lombok.Data;

import java.util.Map;

@Data
public class ExtractorDTO {
    private String name;
    private String sourceField;
    private Map<String, String> properties;


    public VectorExtractor getKeywordExtractor(Dictionary dictionary) {
        if ("uniq_words".equals(this.getName())) {
            return new UniqWordsExtractor(this.getSourceField(), dictionary);
        }
        throw new ApplicationException("No matching extractor");
    }

}
