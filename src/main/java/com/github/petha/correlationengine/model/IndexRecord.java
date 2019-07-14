package com.github.petha.correlationengine.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@Slf4j
@Data
@EqualsAndHashCode
@Builder
public class IndexRecord {

    @NonNull
    private SparseVector vector;

    @NonNull
    private UUID id;

    public void writeToDisc(OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(vector.getPos().length);
        dataOutputStream.writeBoolean(false);
        for (int pos : vector.getPos()) {
            dataOutputStream.writeInt(pos);
        }

        for (int val : vector.getVal()) {
            dataOutputStream.writeInt(val);
        }

        dataOutputStream.writeLong(id.getMostSignificantBits());
        dataOutputStream.writeLong(id.getLeastSignificantBits());
        dataOutputStream.flush();
    }

}
