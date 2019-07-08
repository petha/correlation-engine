package com.github.petha.correlationengine.math;

import com.github.petha.correlationengine.model.IdfContainer;
import com.github.petha.correlationengine.model.SparseVector;
import com.google.common.collect.Sets;

public class CosineSimilarity {
    private CosineSimilarity() {
    }

    public static double score(SparseVector vecA, SparseVector vecB, IdfContainer idf) {
        Sets.SetView<Integer> intersection = Sets.intersection(vecA.getNoZeroElements(), vecB.getNoZeroElements());

        double dotProduct = intersection.stream()
                .map(index -> vecA.get(index) * vecB.get(index) * Math.pow(idf.getIdf(index), 2))
                .reduce(0.0, Double::sum);

        return dotProduct / (Math.sqrt(vecA.norm(idf)) * Math.sqrt(vecB.norm(idf)));
    }


}
