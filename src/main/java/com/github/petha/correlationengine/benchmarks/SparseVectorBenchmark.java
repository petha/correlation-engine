package com.github.petha.correlationengine.benchmarks;

import com.github.petha.correlationengine.model.SparseVector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class SparseVectorBenchmark {
    SparseVector vectorA;
    SparseVector vectorB;

    @Setup
    public void setup() {
        vectorA = new SparseVector();
        vectorB = new SparseVector();
    }
    @Benchmark
    public void testSparseVectorCosineSim() {
    }
}
