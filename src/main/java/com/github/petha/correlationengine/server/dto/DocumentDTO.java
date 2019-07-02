package com.github.petha.correlationengine.server.dto;

import com.github.petha.correlationengine.model.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class DocumentDTO {
    @NotBlank
    private UUID id;
    @NotEmpty
    private Map<String, String> fields = new HashMap<>();

    @JsonIgnore
    public Document getDocument() {
        return Document.builder()
                .id(this.id)
                .fields(this.fields)
                .build();
    }
}
