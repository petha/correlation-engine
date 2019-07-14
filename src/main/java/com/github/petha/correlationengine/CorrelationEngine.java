package com.github.petha.correlationengine;

import com.github.petha.correlationengine.exceptions.ApplicationException;
import com.github.petha.correlationengine.math.CosineSimilarity;
import com.github.petha.correlationengine.model.*;
import com.github.petha.correlationengine.services.DictionaryService;
import com.github.petha.correlationengine.services.FilenameService;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static java.nio.file.StandardOpenOption.READ;

@Slf4j
@Service
public class CorrelationEngine {
    private DictionaryService dictionaryService;
    private FilenameService filenameService;

    private List<Analyzer> analyzerList = new ArrayList<>();
    private HTreeMap<String, byte[]> analyzers;
    private Map<String, ConcurrentMap<UUID, Long>> index = new HashMap<>();
    private DB db;

    public CorrelationEngine(DictionaryService dictionaryService, FilenameService filenameService) {
        this.dictionaryService = dictionaryService;
        this.filenameService = filenameService;

        this.db = DBMaker.fileDB(this.filenameService.getDatabase())
                .fileMmapEnable()
                .transactionEnable()
                .make();

        this.analyzers = this.db.hashMap("analyzers")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();

        this.analyzers.keySet().forEach(name -> this.index.put(name, this.db
                .hashMap(name, Serializer.UUID, Serializer.LONG)
                .createOrOpen()));
    }

    public void addAnalyzer(Analyzer analyzer) {

        boolean analyzerExists = this.analyzerList.stream().anyMatch(existingAnalyzer ->
                existingAnalyzer
                        .getName()
                        .equals(analyzer.getName()));

        if (analyzerExists) {
            throw new ApplicationException("The analyzer already exists");
        }
        this.analyzers.put(analyzer.getName(), "".getBytes());
        this.analyzerList.add(analyzer);
        this.index.put(analyzer.getName(), this.db
                .hashMap(analyzer.getName(), Serializer.UUID, Serializer.LONG)
                .createOrOpen());
    }

    public void analyze(Document document) {
        this.analyzerList
                .parallelStream()
                .forEach(analyzer -> {
                    IndexRecord analyze = analyzer.analyze(document);
                    this.addIndexRecord(analyze, analyzer.getName());
                });
    }

    private synchronized void addIndexRecord(IndexRecord indexRecord, String analyzer) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(
                this.filenameService.getVectorFile(analyzer), true)) {
            long position = fileOutputStream.getChannel().position();
            log.info("Persisting vector for document {} from analyzer", indexRecord.getId(), analyzer);
            indexRecord.writeToDisc(fileOutputStream);
            this.index.get(analyzer).put(indexRecord.getId(), position);
        } catch (IOException e) {
            log.info("Exception thrown", e);
        }
    }

    public int[][] getVector2(UUID sourceId, String analyzer) {
        try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(this.filenameService.getVectorFile(analyzer)))) {
            Long offset = this.index.get(analyzer).get(sourceId);
            if (offset == null) {
                throw new ApplicationException("No vector found");
            }

            while (offset > 0) {
                offset -= fileInputStream.skip(offset);
            }

            return DiskIndexRecordParser.readOneFromStream(fileInputStream);

        } catch (IOException e) {
            log.info("Get vector exception", e);
            throw new ApplicationException("Could not read the stream");
        }
    }

    public List<Correlation> correlateV2(UUID sourceId, String analyzer, float cutOff) {
        int[][] source = getVector2(sourceId, analyzer);
        List<Correlation> correlations = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
        Path path = Paths.get(this.filenameService.getVectorFile(analyzer));
        DiskIndexRecordParser diskIndexRecordParser = new DiskIndexRecordParser();

        try (FileChannel fc = FileChannel.open(path, READ)) {
            while (fc.read(buffer) > 0) {
                buffer.flip();
                diskIndexRecordParser.parse(buffer, (targetValues, id) -> {
                    float score = CosineSimilarity.score(source[0], source[1], targetValues[0], targetValues[1]);
                    if (score >= cutOff) {
                        correlations.add(Correlation.builder()
                                .score(score)
                                .sourceId(sourceId)
                                .targetId(id).build());
                    }
                });

                buffer.compact();
            }
        } catch (IOException e) {
            log.info("Exception reading the file storage for vectors", e);
            throw new ApplicationException("Could not read the vector file");
        }


        correlations.sort(Comparator.comparingDouble(Correlation::getScore).reversed());
        return correlations;
    }

    public List<Analyzer> getAnalyzers() {
        return Collections.unmodifiableList(this.analyzerList);
    }
}
