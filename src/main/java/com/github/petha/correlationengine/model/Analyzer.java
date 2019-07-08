package com.github.petha.correlationengine.model;

import com.github.petha.correlationengine.extractor.VectorExtractor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Analyzer {
    @NonNull
    @Builder.Default
    private List<VectorExtractor> extractorList = new ArrayList<>();

    @NonNull
    Dictionary dictionary;

    @NonNull
    private String name;

    public synchronized IndexRecord analyze(final Document document) {
        log.trace("Analyze document {}", document.getId());
        SparseVector vector = this.extractorList.stream()
                .map(ve -> ve.extract(document))
                .reduce(new SparseVector(), SparseVector::merge);

        this.dictionary.updateTermFrequency(vector);

        return IndexRecord.builder()
                .id(document.getId())
                .name(this.getName())
                .vector(vector)
                .build();
    }
}
