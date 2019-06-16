package Correlation.Extractor;

import Correlation.Model.Document;

import java.util.stream.Stream;

public abstract class VectorExtractor {
    private String sourceField;

    VectorExtractor(String sourceField) {
        this.sourceField = sourceField;
    }

    String getContent(Document document) {
        return document.getFields().getOrDefault(this.sourceField, "");
    }

    public abstract Stream<Integer> extract(Document document);
    public void printStatistics(){}
}
