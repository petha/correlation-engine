package com.github.petha.correlationengine.model;

public interface IdfContainer {
    double getIdf(Integer index);
    void updateTermFrequency(SparseVector vector);
}
