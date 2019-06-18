package Correlation.Model;

import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermFrequency {
    private Map<Integer, Integer> vector = new HashMap<>();

    public void addIndexReqord(IndexRecord indexRecord) {
        @NonNull List<Integer> vector = indexRecord.getVector();
        for (int i = 0; i < vector.size(); i++) {
            Integer count = vector.get(i) >= 1 ? 1 : 0;
            this.vector.compute(i, (key, va) -> va == null ? count : count + va);
        }
    }

    // Calculate term frequency for index, meaning, usual words will be supressed
    // log(corpus length / term appearance)
    // TODO: change TfIdf to be aware of which extractor dictionary used
    public double getTfIdf(int idx, long documents) {
        return Math.log(documents / (this.vector.get(idx)));
    }

    public void printStatistics(String k) {
        System.out.println(
                String.format(
                        "TF for index %s: Dict: %s", k, this.vector));
    }
}
