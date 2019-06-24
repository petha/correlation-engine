package Correlation.Model;

import com.google.common.collect.Sets;
import correlation.protobufs.Protobufs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SparseVector {
    // Term Index -> Frequency
    private HashMap<Integer, Integer> values = new HashMap<>();

    public SparseVector() {
    }

    public SparseVector(Map<Integer, Integer> vector) {
        this.values = new HashMap<>(vector);
    }

    public Integer get(int index) {
        return this.values.get(index);
    }

    public Set<Integer> getSetTerms() {
        return this.values.keySet();
    }

    public void put(int index, int value) {
        this.values.put(index, value);
    }

    public void increment(int index, int value) {
        if (value > 0) {
            this.values.compute(index, (key, previousValue) -> previousValue == null ? value : value + previousValue);
        }
    }

    private double norm() {
        Dictionary dictionary = Dictionary.getInstance();
        return this.values.entrySet().stream()
                .map(entry -> entry.getValue() * dictionary.getIdf(entry.getKey()))
                .reduce(0.0, (acc, val) -> acc + Math.pow(val, 2));
    }

    public SparseVector merge(SparseVector that) {
        that.values.forEach(this::increment);
        return this;
    }

    public double cosineSimilarity(SparseVector that) {
        Sets.SetView<Integer> intersection = Sets.intersection(this.values.keySet(), that.values.keySet());
        Dictionary dictionary = Dictionary.getInstance();
        double dotProduct = intersection.stream()
                .map(index -> this.values.get(index) * that.values.get(index) * Math.pow(dictionary.getIdf(index), 2))
                .reduce(0.0, Double::sum);

        return dotProduct / (Math.sqrt(this.norm()) * Math.sqrt(that.norm()));
    }

    public Protobufs.SparseVector getAsProtobuf() {
        return Protobufs.SparseVector.newBuilder()
                .putAllVector(values)
                .build();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
