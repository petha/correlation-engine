package Correlation;

import Correlation.Model.Analyzer;
import Correlation.Model.Correlation;
import Correlation.Model.Document;
import Correlation.Model.IndexRecord;
import correlation.protobufs.Protobufs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CorrelationEngine {

    @NonNull
    private List<Analyzer> analyzerList = new ArrayList<>();
    private HTreeMap<String, byte[]> analyzers;
    private Map<String, ConcurrentMap<UUID, Long>> index = new HashMap<>();
    private DB db;

    public CorrelationEngine() {
        this.db = DBMaker.fileDB("data.db")
                .fileMmapEnable()
                .make();

        this.analyzers = this.db.hashMap("analyzers")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();

        this.analyzers.keySet().forEach(name -> this.index.put(name, this.db
                .hashMap(name, Serializer.UUID, Serializer.LONG)
                .createOrOpen()));
    }

    public Stream<IndexRecord> getIndex(FileInputStream fileInput) {
        return Stream.generate(() -> {
            try {
                Protobufs.IndexRecord indexRecord = Protobufs.IndexRecord.parseDelimitedFrom(fileInput);
                if (indexRecord == null) return null;
                return IndexRecord.fromProtobuf(indexRecord);
            } catch (IOException e) {
                return null;
            }
        });
    }

    public Set<String> getIndexNames() {
        return Collections.unmodifiableSet(this.analyzers.getKeys());
    }

    public void addAnalyzer(Analyzer analyzer) throws Exception {

        boolean analyzerExists = this.analyzerList.stream().anyMatch(existingAnalyzer ->
                existingAnalyzer
                        .getName()
                        .equals(analyzer.getName()));

        if (analyzerExists) {
            throw new Exception("The analyzer already exists");
        }
        this.analyzers.put(analyzer.getName(), "".getBytes());
        this.analyzerList.add(analyzer);
        this.index.put(analyzer.getName(), this.db
                .hashMap(analyzer.getName(), Serializer.UUID, Serializer.LONG)
                .createOrOpen());
    }

    public void analyze(Document document) {
        this.analyzerList.parallelStream()
                .map(analyzer -> analyzer.analyze(document))
                .forEach(this::addIndexRecord);
    }

    public void printStatistics() {
        this.analyzerList.forEach(Analyzer::printStatistics);
    }

    private synchronized void addIndexRecord(IndexRecord indexRecord) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(indexRecord.getName(), true)) {
            long position = fileOutputStream.getChannel().position();
            log.info("Indexing document {} ", indexRecord.getId());
            indexRecord.getAsProtobuf().writeDelimitedTo(fileOutputStream);
            this.index.get(indexRecord.getName()).put(indexRecord.getId(), position);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<IndexRecord> getVector(UUID sourceId, String analyzer) {
        try (FileInputStream fileInputStream = new FileInputStream(analyzer)) {
            Long offset = this.index.get(analyzer).get(sourceId);
            if (offset == null) {
                return Optional.empty();
            }

            fileInputStream.skip(offset);
            Protobufs.IndexRecord indexRecord = Protobufs.IndexRecord.parseDelimitedFrom(fileInputStream);
            if (indexRecord == null) return Optional.empty();
            return Optional.of(IndexRecord.fromProtobuf(indexRecord));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read the stream");
        }
    }

    public List<Correlation> correlate(UUID sourceId, String analyzer, double cutOff) {
        return this.getVector(sourceId, analyzer)
                .map(v -> this.correlate(v, cutOff))
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }

    public List<Correlation> correlate(IndexRecord source, double cutOff) {
        try (FileInputStream fileInputStream = new FileInputStream(source.getName())) {
            return this.getIndex(fileInputStream)
                    .takeWhile(Objects::nonNull)
                    .filter(indexRecord -> !indexRecord.getId().equals(source.getId()))
                    .map(target -> this.correlate(source, target))
                    .filter(correlation -> correlation.getScore() > cutOff)
                    .sorted(Comparator.comparingDouble(Correlation::getScore).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not read the stream");
        }
    }

    public List<Analyzer> getAnalyzers() {
        return Collections.unmodifiableList(this.analyzerList);
    }

    private Correlation correlate(IndexRecord source, IndexRecord target) {
        return Correlation.builder()
                .sourceId(source.getId())
                .targetId(target.getId())
                .score(source.getVector().cosineSimilarity(target.getVector()))
                .build();
    }

    public void shutdown() {
        this.db.commit();
        this.db.close();
    }
}
