import Model.Correlation;
import Model.Document;
import Model.IndexRecord;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor
public class CorrelationEngine {

    @NonNull
    private List<Analyzer> analyzerList = new ArrayList<>();

    private Map<String, Set<IndexRecord>> indices = new HashMap<>();

    // TODO: Handle different length of indices by making each extractor output to a set of results
    private static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        int length = Integer.max(vectorA.size(), vectorB.size());

        for (int i = 0; i < length; i++) {
            double a = i < vectorA.size() ? vectorA.get(i) : 0;
            double b = i < vectorB.size() ? vectorB.get(i) : 0;

            dotProduct += a * b;
            normA += Math.pow(a, 2);
            normB += Math.pow(b, 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public Set<IndexRecord> getIndex(String name) {
        return Collections.unmodifiableSet(this.indices.get(name));
    }

    public void addAnalyzer(Analyzer analyzer) {
        this.analyzerList.add(analyzer);
    }

    public void analyze(Document document) {
        this.analyzerList.parallelStream()
                .map(analyzer -> analyzer.analyze(document))
                .forEach(this::addIndexRecord);

    }

    public void printIndicesStatistics() {
        this.indices.forEach((k, v) -> System.out.println(String.format("Index: \"%s\" documents: %d", k, v.size())));
    }

    private void addIndexRecord(IndexRecord indexRecord) {
        Set<IndexRecord> indexRecords = this.indices
                .getOrDefault(indexRecord.getName(), new HashSet<>());

        indexRecords.add(indexRecord);
        this.indices.put(indexRecord.getName(), indexRecords);
    }

    public Stream<Correlation> correlate(IndexRecord source, double cutOff) {
        return indices.getOrDefault(source.getName(), new HashSet<>()).parallelStream()
                .filter(indexRecord -> !indexRecord.getId().equals(source.getId()))
                .map(target -> this.correlate(source, target))
                .filter(correlation -> correlation.getScore() > cutOff)
                .sorted(Comparator.comparingDouble(Correlation::getScore).reversed());
    }

    public Correlation correlate(IndexRecord source, IndexRecord target) {
        return Correlation.builder()
                .sourceId(source.getId())
                .targetId(target.getId())
                .score(CorrelationEngine.cosineSimilarity(source.getVector(), target.getVector()))
                .build();
    }

}
