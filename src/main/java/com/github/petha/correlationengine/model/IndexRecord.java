package com.github.petha.correlationengine.model;

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
    private UUID id;

    public static IndexRecord fromProtobuf(Protobufs.IndexRecord indexRecord) {
        return IndexRecord.builder()
                .id(UUID.fromString(indexRecord.getId()))
                .vector(new SparseVector(indexRecord.getVector().getPosList(), indexRecord.getVector().getValList()))
                .build();
    }

    public Protobufs.IndexRecord getAsProtobuf() {
        return Protobufs.IndexRecord.newBuilder()
                .setId(id.toString())
                .setVector(vector.getAsProtobuf())
                .build();
    }
}
