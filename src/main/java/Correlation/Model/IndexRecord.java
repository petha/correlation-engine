package Correlation.Model;

import correlation.protobufs.Protobufs;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Data
@Builder
public class IndexRecord {

    @NonNull
    private SparseVector vector;
    @NonNull
    private String name;
    @NonNull
    private UUID id;

    public Protobufs.IndexRecord getAsProtobuf() {
        return Protobufs.IndexRecord.newBuilder()
                .setId(id.toString())
                .setVector(vector.getAsProtobuf())
                .setName(name)
                .build();
    }

    public static IndexRecord fromProtobuf(Protobufs.IndexRecord indexRecord) {
        return IndexRecord.builder()
                .id(UUID.fromString(indexRecord.getId()))
                .name(indexRecord.getName())
                .vector(new SparseVector(indexRecord.getVector().getVectorMap()))
                .build();
    }
}
