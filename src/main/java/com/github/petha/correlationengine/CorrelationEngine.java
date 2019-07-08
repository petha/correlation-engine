package com.github.petha.correlationengine;

import com.github.petha.correlationengine.exceptions.ApplicationException;
import com.github.petha.correlationengine.math.CosineSimilarity;
import com.github.petha.correlationengine.model.Dictionary;
import com.github.petha.correlationengine.model.*;
import com.github.petha.correlationengine.services.DictionaryService;
import com.github.petha.correlationengine.services.FilenameService;
import correlation.protobufs.Protobufs;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this.analyzerList.parallelStream()
                .map(analyzer -> analyzer.analyze(document))
                .forEach(this::addIndexRecord);
    }


    private synchronized void addIndexRecord(IndexRecord indexRecord) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(
                this.filenameService.getVectorFile(indexRecord.getName()), true)) {
            long position = fileOutputStream.getChannel().position();
            log.info("Persisting vector for document {} from analyzer", indexRecord.getId(), indexRecord.getName());
            indexRecord.getAsProtobuf().writeDelimitedTo(fileOutputStream);
            this.index.get(indexRecord.getName()).put(indexRecord.getId(), position);
        } catch (IOException e) {
            log.info("Exception thrown", e);
        }
    }

    public Optional<IndexRecord> getVector(UUID sourceId, String analyzer) {
        try (FileInputStream fileInputStream = new FileInputStream(this.filenameService.getVectorFile(analyzer))) {
            Long offset = this.index.get(analyzer).get(sourceId);
            if (offset == null) {
                return Optional.empty();
            }

            while (offset > 0) {
                offset -= fileInputStream.skip(offset);
            }

            Protobufs.IndexRecord indexRecord = Protobufs.IndexRecord.parseDelimitedFrom(fileInputStream);
            if (indexRecord == null) return Optional.empty();
            return Optional.of(IndexRecord.fromProtobuf(indexRecord));

        } catch (IOException e) {
            log.info("Get vector exception", e);
            throw new ApplicationException("Could not read the stream");
        }
    }

    public List<Correlation> correlate(UUID sourceId, String analyzer, double cutOff) {
        return this.getVector(sourceId, analyzer)
                .map(v -> this.correlate(v, cutOff))
                .orElseThrow(() -> new ApplicationException("Record not found"));
    }

    public List<Correlation> correlate(IndexRecord source, double cutOff) {
        Dictionary dictionary = this.dictionaryService.getDictionary(source.getName());
        try (FileInputStream fileInputStream = new FileInputStream(this.filenameService.getVectorFile(source.getName()))) {
            return this.getIndex(fileInputStream)
                    .takeWhile(Objects::nonNull)
                    .filter(indexRecord -> !indexRecord.getId().equals(source.getId()))
                    .map(target -> this.correlate(source, target, dictionary))
                    .filter(correlation -> correlation.getScore() > cutOff)
                    .sorted(Comparator.comparingDouble(Correlation::getScore).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ApplicationException("Could not read the stream");
        }
    }

    public List<Analyzer> getAnalyzers() {
        return Collections.unmodifiableList(this.analyzerList);
    }

    private Correlation correlate(IndexRecord source, IndexRecord target, Dictionary dictionary) {
        return Correlation.builder()
                .sourceId(source.getId())
                .targetId(target.getId())
                .score(CosineSimilarity.score(source.getVector(), target.getVector(), dictionary))
                .build();
    }
}
