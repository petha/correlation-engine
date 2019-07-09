package com.github.petha.correlationengine.model;

import correlation.protobufs.Protobufs;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class SparseVector {
    // Term Index -> Frequency
    private SortedList sortedList = new SortedList();
    private List<Integer> values = new ArrayList<>();

    public SparseVector(Map<Integer, Integer> vector) {
        vector.forEach(this::increment);
    }

    public int get(int index) {
        int contains = this.sortedList.contains(index);
        if (contains >= 0) {
            return this.values.get(contains);
        }
        return 0;
    }

    public Set<Integer> getSetTerms() {
        return this.sortedList.asSet();
    }

    public void increment(int index, int value) {
        if (value > 0) {
            int contains = this.sortedList.contains(index);
            if (contains >= 0) {
                Integer integer = this.values.get(contains);
                this.values.set(contains, integer + value);
            } else {
                int insert = this.sortedList.insert(index);
                this.values.add(insert, value);
            }
        }
    }

    public int[] getDense(int dimension) {
        int[] dense = new int[dimension];
        List<Integer> list = this.sortedList.getList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            int index = list.get(i);
            Integer value = this.values.get(i);
            dense[index] = value;
        }
        return dense;
    }

    public float norm(IdfContainer dictionary) {
        return this.values.stream()
                .map(Integer::floatValue)
                .reduce(0.0f, (acc, val) -> acc + (float) Math.pow(val, 2));
    }

    public SparseVector merge(SparseVector that) {
        int size = that.sortedList.size();
        for (int i = 0; i < size; i++) {
            int i1 = that.sortedList.get(i);
            this.increment(i1, that.values.get(i));
        }
        return this;
    }

    public Protobufs.SparseVector getAsProtobuf() {
        HashMap<Integer, Integer> integerIntegerHashMap = new HashMap<>();
        int size = this.sortedList.size();
        for (int i = 0; i < size; i++) {
            int index = this.sortedList.get(i);
            Integer value = this.values.get(i);
            integerIntegerHashMap.put(index, value);
        }

        return Protobufs.SparseVector.newBuilder()
                .putAllVector(integerIntegerHashMap)
                .build();
    }
}
