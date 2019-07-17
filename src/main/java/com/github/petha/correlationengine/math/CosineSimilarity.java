package com.github.petha.correlationengine.math;

public class CosineSimilarity {
    private CosineSimilarity() {
    }

    public static float score(int[] posA, int[] valA, int[] posB, int[] valB) {
        int i = 0;
        int j = 0;

        int dotproduct = 0;
        int lengthA = 0;
        int lengthB = 0;
        int length = posA.length;
        int length1 = posB.length;
        while (i < length && j < length1) {
            if (posA[i] < posB[j]) {
                lengthA += Math.pow(valA[i++], 2);
            } else if (posB[j] < posA[i]) {
                lengthB += Math.pow(valB[j++], 2);
            } else {
                lengthA += Math.pow(valA[i], 2);
                lengthB += Math.pow(valB[j], 2);
                dotproduct += valB[j++] * valA[i++];
            }
        }
        return dotproduct / (float) (Math.sqrt(lengthA) * Math.sqrt(lengthB));
    }
}
