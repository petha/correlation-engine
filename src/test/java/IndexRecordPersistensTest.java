import com.github.petha.correlationengine.model.DiskIndexRecordParser;
import com.github.petha.correlationengine.model.IndexRecord;
import com.github.petha.correlationengine.model.SparseVector;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.READ;

public class IndexRecordPersistensTest {

    @Test()
    public void writeAndReadAll() throws IOException {
        List<IndexRecord> recordsToPersist = new ArrayList<>();
        File tempFile = File.createTempFile("index", "vec");
        tempFile.deleteOnExit();
        UUID uuid = UUID.randomUUID();
        for (int i = 0; i < 200; i++) {
            SparseVector sparseVector = new SparseVector();
            for (int j = 0; j < 10; j++) {
                sparseVector.increment(j, j);
            }

            recordsToPersist.add(IndexRecord.builder()
                    .id(uuid)
                    .vector(sparseVector)
                    .build());
        }

        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile, true));

        for (IndexRecord record : recordsToPersist) {
            record.writeToDisc(fileOutputStream);
        }

        fileOutputStream.close();

        // Read back
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 8);
        DiskIndexRecordParser diskIndexRecordParser = new DiskIndexRecordParser();
        ArrayList<IndexRecord> readIndexRecords = new ArrayList<>();

        try (FileChannel fc = FileChannel.open(tempFile.toPath(), READ)) {
            while (fc.read(buffer) > 0) {
                buffer.flip();
                diskIndexRecordParser.parse(buffer, (values, id) -> {
                    SparseVector sparseVector = new SparseVector(values);
                    readIndexRecords.add(IndexRecord.builder().vector(sparseVector).id(uuid).build());
                });
                buffer.compact();
            }
        }

        Assert.assertEquals(recordsToPersist.size(), readIndexRecords.size());
        Assert.assertEquals(recordsToPersist, readIndexRecords);

    }
}
