package com.github.petha.correlationengine.model;

import com.google.common.collect.Sets;
import correlation.protobufs.Protobufs;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class SparseVector {
    // Term Index -> Frequency
    private HashMap<Integer, Integer> values = new HashMap<>();

    public SparseVector(Map<Integer, Integer> vector) {
        this.values = new HashMap<>(vector);
    }

    public Integer get(int index) {
        return this.values.get(index);
    }

    public Set<Integer> getSetTerms() {
        return this.values.keySet();
    }

    public void increment(int index, int value) {
        if (value > 0) {
            this.values.compute(index, (key, previousValue) -> previousValue == null ? value : value + previousValue);
        }
    }

    private double norm(Dictionary dictionary) {
        return this.values.entrySet().stream()
                .map(entry -> entry.getValue() * dictionary.getIdf(entry.getKey()))
                .reduce(0.0, (acc, val) -> acc + Math.pow(val, 2));
    }

    public SparseVector merge(SparseVector that) {
        that.values.forEach(this::increment);
        return this;
    }

    public double cosineSimilarity(SparseVector that, Dictionary dictionary) {
        Sets.SetView<Integer> intersection = Sets.intersection(this.values.keySet(), that.values.keySet());
        double dotProduct = intersection.stream()
                .map(index -> this.values.get(index) * that.values.get(index) * Math.pow(dictionary.getIdf(index), 2))
                .reduce(0.0, Double::sum);

        return dotProduct / (Math.sqrt(this.norm(dictionary)) * Math.sqrt(that.norm(dictionary)));
    }

    public Protobufs.SparseVector getAsProtobuf() {
        return Protobufs.SparseVector.newBuilder()
                .putAllVector(values)
                .build();
    }
}
