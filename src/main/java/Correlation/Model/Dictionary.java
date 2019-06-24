package Correlation.Model;


import java.util.HashMap;

public class Dictionary {
    private static Dictionary instance = new Dictionary();
    private int currentSize = 0;
    private HashMap<String, Integer> terms = new HashMap<>();
    private SparseVector indexDocumentFrequency = new SparseVector();
    private HashMap<Integer, Double> precalculatedIdf = new HashMap<>();
    private int documents;

    private Dictionary() {
    }

    public static Dictionary getInstance() {
        return instance;
    }

    public synchronized void add(String term) {
        if (!terms.containsKey(term)) {
            this.terms.put(term, currentSize++);
        }
    }

    public synchronized void updateTermFrequency(SparseVector vector) {
        vector.getSetTerms()
                .forEach(index ->
                        this.indexDocumentFrequency.increment(index, 1));
        this.documents += 1;
        this.resetIdf();
    }

    public synchronized double getIdf(Integer index) {
        return this.precalculatedIdf
                .computeIfAbsent(index, idx -> Math.log((double)this.documents / (double)this.indexDocumentFrequency.get(idx)));
    }

    private synchronized void resetIdf() {
        this.precalculatedIdf.clear();
    }

    public synchronized Integer getIndex(String term) {
        return terms.get(term);
    }

    @Override
    public String toString() {
        return terms.toString();
    }
}
