package Model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class Document {
    private UUID id;
    private String typeName;
    private Map<String, String> fields;
}
