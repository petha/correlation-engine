package com.github.petha.correlationengine.benchmarks;

import com.github.petha.correlationengine.math.CosineSimilarity;
import com.github.petha.correlationengine.model.IdfContainer;
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
        int dictionaryLength = 3000;
        vectorA = new SparseVector();
        vectorB = new SparseVector();
        for (int i = 0; i < 5000; i++) {
            vectorA.increment((int) Math.round(Math.random() * dictionaryLength), 1);
            vectorB.increment((int) Math.round(Math.random() * dictionaryLength), 1);
        }
    }

    @Benchmark
    public void denseConversion() {
        vectorA.getDense(3001);
        vectorB.getDense(3001);
        vectorB.getDense(3001);
    }

    @Benchmark
    public void testSparseVectorCosineSim() {

        CosineSimilarity.score(vectorA, vectorB, new IdfContainer() {
            @Override
            public synchronized float getIdf(Integer index) {
                return 1;
            }

            @Override
            public void updateTermFrequency(SparseVector vector) {

            }
        });
    }
}
