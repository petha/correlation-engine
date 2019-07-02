package com.github.petha.correlationengine.server.dto;

import com.github.petha.correlationengine.model.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DocumentDTO {
    private Map<String, String> fields;

    @JsonIgnore
    public Document getDocument() {
        return Document.builder()
                .id(UUID.randomUUID())
                .fields(fields)
                .build();
    }
}
