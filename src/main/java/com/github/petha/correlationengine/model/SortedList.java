package com.github.petha.correlationengine.model;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@EqualsAndHashCode
public class SortedList {
    List<Integer> list = new ArrayList<>();

    public int insert(int value) {
        int insertIndex = binarySearch(value);
        list.add(insertIndex, value);
        return insertIndex;
    }

    public List<Integer> getList() {
        return this.list;
    }

    public Set<Integer> asSet() {
        return new HashSet<>(list);
    }

    public Stream<Integer> stream() {
        return this.list.stream();
    }

    private int binarySearch(int check) {
        int hi = this.list.size() - 1;
        int lo = 0;
        while (hi >= lo) {
            int guess = (lo + hi) >>> 1;
            if (this.list.get(guess) >= check)
                hi = guess - 1;
            else
                lo = guess + 1;
        }
        return lo;
    }

    public int contains(int check) {
        int hi = this.list.size() - 1;
        int lo = 0;
        while (hi >= lo) {
            int guess = (lo + hi) >>> 1;
            int current = this.list.get(guess);
            if (current > check) {
                hi = guess - 1;
            } else if (current < check) {
                lo = guess + 1;
            } else {
                return guess;
            }
        }
        return -1;
    }

    public int size() {
        return this.list.size();
    }

    public int get(int index) {
        return list.get(index);
    }
}
