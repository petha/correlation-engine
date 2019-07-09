package com.github.petha.correlationengine.math;

import com.github.petha.correlationengine.model.IdfContainer;
import com.github.petha.correlationengine.model.SparseVector;
import com.google.common.collect.Sets;

public class CosineSimilarity {
    private CosineSimilarity() {
    }

    public static float score(SparseVector vecA, SparseVector vecB, IdfContainer idf) {
        Sets.SetView<Integer> intersection = Sets.intersection(vecA.getSetTerms(), vecB.getSetTerms());

        float dotProduct = intersection.stream()
                .map(index -> (float)vecA.get(index) * vecB.get(index))
                .reduce(0.0f, Float::sum);

        return dotProduct / (float) (Math.sqrt(vecA.norm(idf)) * Math.sqrt(vecB.norm(idf)));
    }


}
