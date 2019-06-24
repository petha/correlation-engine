package Correlation.Model;


import correlation.protobufs.Protobufs;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class Dictionary {
    private static Dictionary instance = new Dictionary();
    private int currentIndex = 0;
    private HashMap<String, Integer> terms = new HashMap<>();
    private SparseVector indexDocumentFrequency = new SparseVector();
    private HashMap<Integer, Double> precalculatedIdf = new HashMap<>();
    private int documents;
    private FileOutputStream termStorage;

    private Dictionary() {
        String fileName = "dictionary";
        this.readDictionaryTerms(fileName);
        this.readDictionaryIdf(fileName);

        try {
            this.termStorage = new FileOutputStream(String.format("%s.terms", fileName), true);
        } catch (IOException e) {
            throw new RuntimeException("The term database could not be opened");
        }
    }

    public static Dictionary getInstance() {
        return instance;
    }

    private void readDictionaryTerms(String fileName) throws RuntimeException {
        try (FileInputStream fileInputStream = new FileInputStream(String.format("%s.terms", fileName))) {
            while (true) {
                Protobufs.Term term = Protobufs.Term.parseDelimitedFrom(fileInputStream);
                if (term == null) break;
                this.terms.put(term.getName(), term.getId());
            }
            log.info("Read {} terms", this.terms.size());
        } catch (FileNotFoundException e) {
            log.info("The dictionary database was not found");
        } catch (IOException e) {
            throw new RuntimeException("Error reading the dictionary");
        }
    }

    private void readDictionaryIdf(String fileName) throws RuntimeException {
        try (FileInputStream fileInputStream = new FileInputStream(String.format("%s.idf", fileName))) {
            Protobufs.DocumentFrequency documentFrequency = Protobufs.DocumentFrequency.parseFrom(fileInputStream);
            this.documents = documentFrequency.getDocuments();
            this.indexDocumentFrequency = new SparseVector(documentFrequency.getFrequency().getVectorMap());
        } catch (FileNotFoundException e) {
            log.info("The document frequency was not found");
        } catch (IOException e) {
            throw new RuntimeException("Error reading the document frequency table");
        }
    }

    private synchronized void writeIndexDocumentFrequency(String filename) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s.idf", filename), false);
            Protobufs.DocumentFrequency build = Protobufs.DocumentFrequency.newBuilder()
                    .setDocuments(this.documents)
                    .setFrequency(this.indexDocumentFrequency.getAsProtobuf())
                    .build();
            build.writeTo(fileOutputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public synchronized void add(String term) {
        if (!terms.containsKey(term)) {

            Protobufs.Term build = Protobufs.Term.newBuilder()
                    .setName(term)
                    .setId(currentIndex)
                    .build();
            try {
                build.writeDelimitedTo(this.termStorage);
            } catch (IOException e) {
                throw new RuntimeException("The term database could not be written to");
            }
            this.terms.put(term, currentIndex++);
        }
    }

    public synchronized void updateTermFrequency(SparseVector vector) {
        vector.getSetTerms()
                .forEach(index ->
                        this.indexDocumentFrequency.increment(index, 1));
        this.documents += 1;
        this.resetIdf();
        this.writeIndexDocumentFrequency("dictionary");
    }

    public synchronized double getIdf(Integer index) {
        return this.precalculatedIdf
                .computeIfAbsent(index, idx -> Math.log((double) this.documents / (double) this.indexDocumentFrequency.get(idx)));
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
