package Correlation;

import Correlation.Model.Analyzer;
import Correlation.Model.Correlation;
import Correlation.Model.Document;
import Correlation.Model.IndexRecord;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor
public class CorrelationEngine {

    @NonNull
    private List<Analyzer> analyzerList = new ArrayList<>();

    private Map<String, Set<IndexRecord>> indices = new HashMap<>();

    public Set<IndexRecord> getIndex(String name) {
        return Collections.unmodifiableSet(this.indices.get(name));
    }

    public Set<String> getIndexNames() {
        return Collections.unmodifiableSet(this.indices.keySet());
    }

    public void addAnalyzer(Analyzer analyzer) throws Exception {

        boolean analyzerExists = this.analyzerList.stream().anyMatch(existingAnalyzer ->
                existingAnalyzer
                        .getName()
                        .equals(analyzer.getName()));

        if (analyzerExists) {
            throw new Exception("The analyzer already exists");
        }

        this.analyzerList.add(analyzer);
    }

    public void analyze(Document document) {
        this.analyzerList.parallelStream()
                .map(analyzer -> analyzer.analyze(document))
                .forEach(this::addIndexRecord);
    }

    public void printStatistics() {
        this.indices.forEach((k, v) -> System.out.println(String.format("Index: \"%s\" documents: %d", k, v.size())));
        this.analyzerList.forEach(Analyzer::printStatistics);
    }

    private void addIndexRecord(IndexRecord indexRecord) {
        Set<IndexRecord> indexRecords = this.indices
                .getOrDefault(indexRecord.getName(), new HashSet<>());

        indexRecords.add(indexRecord);

        this.indices.put(indexRecord.getName(), indexRecords);
    }

    public Stream<Correlation> correlate(UUID sourceId, String analyzer, double cutOff) {
        //TODO: Use hash map for finding indexes
        return this.getIndex(analyzer)
                .stream()
                .filter(r -> r.getId().equals(sourceId))
                .findFirst()
                .map(record -> this.correlate(record, cutOff))
                .orElseThrow(RuntimeException::new);
    }

    public Stream<Correlation> correlate(IndexRecord source, double cutOff) {
        return indices.getOrDefault(source.getName(), new HashSet<>()).parallelStream()
                .filter(indexRecord -> !indexRecord.getId().equals(source.getId()))
                .map(target -> this.correlate(source, target))
                .filter(correlation -> correlation.getScore() > cutOff)
                .sorted(Comparator.comparingDouble(Correlation::getScore).reversed());
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

}
