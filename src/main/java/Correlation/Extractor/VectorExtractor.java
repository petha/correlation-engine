package Correlation.Extractor;

import Correlation.Model.Document;
import Correlation.Model.SparseVector;

public abstract class VectorExtractor {
    private String sourceField;

    VectorExtractor(String sourceField) {
        this.sourceField = sourceField;
    }

    String getContent(Document document) {
        return document.getFields().getOrDefault(this.sourceField, "");
    }

    public abstract SparseVector extract(Document document);

    public void printStatistics() {
    }
}
