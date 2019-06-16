package Model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class IndexRecord {
    // TODO: Change vector into something that are aware of which extractor is creating the part
    @NonNull
    private List<Integer> vector;
    @NonNull
    private String name;
    @NonNull
    private UUID id;
}
