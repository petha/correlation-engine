package Correlation;

import Correlation.Model.Correlation;
import Correlation.Model.Document;
import Correlation.Model.IndexRecord;
import Correlation.Model.TermFrequency;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@NoArgsConstructor
public class CorrelationEngine {

    @NonNull
    private List<Analyzer> analyzerList = new ArrayList<>();

    private Map<String, Set<IndexRecord>> indices = new HashMap<>();
    private Map<String, TermFrequency> termFrequencies = new ConcurrentHashMap<>();

    // Calculate term frequency for index, meaning, usual words will be supressed
    // log(corpus length / term appearance)
    private long documents = 0;

    // TODO: Handle different length of indices by making each extractor output to a set of results
    private double cosineSimilarity(List<Integer> vectorA, List<Integer> vectorB, TermFrequency tf) {

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        int sizeA = vectorA.size();
        int sizeB = vectorB.size();
        int length = Integer.max(sizeA, sizeB);

        for (int i = 0; i < length; i++) {
            double a = i < sizeA ? vectorA.get(i) : 0;
            double b = i < sizeB ? vectorB.get(i) : 0;
            double tfidf = tf.getTfIdf(i, this.documents);
            a *= tfidf;
            b *= tfidf;

            dotProduct += a * b;
            normA += Math.pow(a, 2);
            normB += Math.pow(b, 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public Set<IndexRecord> getIndex(String name) {
        return Collections.unmodifiableSet(this.indices.get(name));
    }

    public Set<String> getIndexNames() {
        return Collections.unmodifiableSet(this.indices.keySet());
    }

    public void addAnalyzer(Analyzer analyzer) {
        // TODO: Verify that analyzer is not existing
        this.analyzerList.add(analyzer);
    }

    public void analyze(Document document) {
        this.analyzerList.parallelStream()
                .map(analyzer -> analyzer.analyze(document))
                .filter(record -> record.getVector().stream().anyMatch(d -> d != 0))
                .forEach(this::addIndexRecord);
        this.documents += 1;
    }

    public void printStatistics() {
        this.indices.forEach((k, v) -> System.out.println(String.format("Index: \"%s\" documents: %d", k, v.size())));
        this.termFrequencies.forEach((k, tf) -> tf.printStatistics(k));
        this.analyzerList.forEach(Analyzer::printStatistics);

    }

    private void addIndexRecord(IndexRecord indexRecord) {
        Set<IndexRecord> indexRecords = this.indices
                .getOrDefault(indexRecord.getName(), new HashSet<>());

        indexRecords.add(indexRecord);
        this.termFrequencies.computeIfAbsent(indexRecord.getName(), string -> new TermFrequency())
                .addIndexReqord(indexRecord);

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

    public Correlation correlate(IndexRecord source, IndexRecord target) {
        return Correlation.builder()
                .sourceId(source.getId())
                .targetId(target.getId())
                .score(this.cosineSimilarity(
                        source.getVector(),
                        target.getVector(),
                        this.termFrequencies.get(source.getName())))
                .build();
    }

}
