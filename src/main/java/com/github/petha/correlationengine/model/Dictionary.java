package com.github.petha.correlationengine.model;


import com.github.petha.correlationengine.exceptions.ApplicationException;
import correlation.protobufs.Protobufs;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class Dictionary {
    private int currentIndex = 0;
    private HashMap<String, Integer> terms = new HashMap<>();
    private SparseVector indexDocumentFrequency = new SparseVector();
    private HashMap<Integer, Double> preCalculatedIdf = new HashMap<>();
    private int documents;
    private FileOutputStream termStorage;
    private String filename;

    public Dictionary(String filename) {
        this.filename = filename;
        this.readDictionaryTerms();
        this.readDictionaryIdf();

        try {
            this.termStorage = new FileOutputStream(String.format("%s.terms", filename), true);
        } catch (IOException e) {
            throw new ApplicationException("The term database could not be opened");
        }
    }

    private void readDictionaryTerms() {
        try (FileInputStream fileInputStream = new FileInputStream(String.format("%s.terms", this.filename))) {
            while (true) {
                Protobufs.Term term = Protobufs.Term.parseDelimitedFrom(fileInputStream);
                if (term == null) break;
                this.terms.put(term.getName(), term.getId());
            }
            log.info("Read {} terms", this.terms.size());
            // Reset current index
        } catch (FileNotFoundException e) {
            log.info("The dictionary database was not found");
        } catch (IOException e) {
            throw new ApplicationException("Error reading the dictionary");
        }
    }

    private void readDictionaryIdf() {
        try (FileInputStream fileInputStream = new FileInputStream(String.format("%s.idf", this.filename))) {
            Protobufs.DocumentFrequency documentFrequency = Protobufs.DocumentFrequency.parseFrom(fileInputStream);
            this.documents = documentFrequency.getDocuments();
            this.indexDocumentFrequency = new SparseVector(documentFrequency.getFrequency().getVectorMap());
            log.info("IDF read. Terms {}, Indexed documents: {}", this.indexDocumentFrequency.getSetTerms().size(), this.documents);
        } catch (FileNotFoundException e) {
            log.info("The document frequency was not found");
        } catch (IOException e) {
            throw new ApplicationException("Error reading the document frequency table");
        }
    }

    private synchronized void writeIndexDocumentFrequency() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s.idf", this.filename), false);
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
                throw new ApplicationException("The term database could not be written to");
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
        this.writeIndexDocumentFrequency();
    }

    public synchronized double getIdf(Integer index) {
        return this.preCalculatedIdf
                .computeIfAbsent(index, idx -> Math.log((double) this.documents / (double) this.indexDocumentFrequency.get(idx)));
    }

    private synchronized void resetIdf() {
        this.preCalculatedIdf.clear();
    }

    public synchronized Integer getIndex(String term) {
        return terms.get(term);
    }

    @Override
    public String toString() {
        return terms.toString();
    }
}
