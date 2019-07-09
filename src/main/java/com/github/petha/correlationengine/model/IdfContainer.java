package com.github.petha.correlationengine.model;

public interface IdfContainer {
    float getIdf(Integer index);
    void updateTermFrequency(SparseVector vector);
}
