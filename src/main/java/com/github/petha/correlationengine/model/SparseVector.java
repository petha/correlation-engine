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

    public Set<Integer> getNoZeroElements() {
        return this.values.keySet();
    }

    public double norm(IdfContainer dictionary) {
        return this.values.entrySet().stream()
                .map(entry -> entry.getValue() * dictionary.getIdf(entry.getKey()))
                .reduce(0.0, (acc, val) -> acc + Math.pow(val, 2));
    }

    public SparseVector merge(SparseVector that) {
        that.values.forEach(this::increment);
        return this;
    }

    public Protobufs.SparseVector getAsProtobuf() {
        return Protobufs.SparseVector.newBuilder()
                .putAllVector(values)
                .build();
    }
}
