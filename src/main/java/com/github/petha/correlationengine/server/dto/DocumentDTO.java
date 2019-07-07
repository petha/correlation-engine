package com.github.petha.correlationengine.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.petha.correlationengine.model.Document;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class DocumentDTO {
    @NotNull
    private UUID id;

    @Valid
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
