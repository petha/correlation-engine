package Server.DTO;

import Correlation.Model.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DocumentDTO {
    private String typeName;
    private Map<String, String> fields;

    @JsonIgnore
    public Document getDocument() {
        return Document.builder()
                .id(UUID.randomUUID())
                .fields(fields)
                .typeName(typeName)
                .build();
    }
}
