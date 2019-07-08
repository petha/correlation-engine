package com.github.petha.correlationengine.extractor;

import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.model.SparseVector;

public abstract class VectorExtractor {
    private String sourceField;

    VectorExtractor(String sourceField) {
        this.sourceField = sourceField;
    }

    String getContent(Document document) {
        return document.getFields().getOrDefault(this.sourceField, "");
    }

    public abstract SparseVector extract(Document document);

}
