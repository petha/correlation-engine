package com.github.petha.correlationengine.benchmarks;

import com.github.petha.correlationengine.math.CosineSimilarity;
import com.github.petha.correlationengine.model.SparseVector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class SparseVectorBenchmark {
    SparseVector vectorA;
    SparseVector vectorB;
    int[] posA;
    int[] posB;
    int[] valA;
    int[] valB;

    @Setup
    public void setup() {
        int dictionaryLength = 1500;
        vectorA = new SparseVector();
        vectorB = new SparseVector();
        for (int i = 0; i < 90; i++) {
            vectorA.increment((int) Math.round(Math.random() * dictionaryLength), 1);
            vectorB.increment((int) Math.round(Math.random() * dictionaryLength), 1);
        }
        this.posA = vectorA.getPos();
        this.valA = vectorA.getVal();

        this.posB = vectorB.getPos();
        this.valB = vectorB.getVal();

    }

    @Benchmark
    public void testSparseVectorCosineSim(Blackhole blackhole) {
        blackhole.consume(CosineSimilarity.score(posA, valA, posB, valB));
    }
}
