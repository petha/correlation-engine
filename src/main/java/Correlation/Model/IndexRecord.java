package Correlation.Model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@Builder
public class IndexRecord {
    @NonNull
    private SparseVector vector;
    @NonNull
    private String name;
    @NonNull
    private UUID id;
}
