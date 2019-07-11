package com.github.petha.correlationengine.model;


import com.github.petha.correlationengine.exceptions.ApplicationException;
import com.github.petha.correlationengine.services.FilenameService;
import correlation.protobufs.Protobufs;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class Dictionary implements IdfContainer {
    private int currentIndex = 0;
    private HashMap<String, Integer> terms = new HashMap<>();
    private SparseVector indexDocumentFrequency = new SparseVector();
    private HashMap<Integer, Float> preCalculatedIdf = new HashMap<>();
    private int documents;
    private FileOutputStream termStorage;
    private String name;
    private FilenameService filenameService;

    public Dictionary(String name, FilenameService filenameService) {
        this.name = name;
        this.filenameService = filenameService;

        this.readDictionaryTerms();
        this.readDictionaryIdf();

        try {
            this.termStorage = new FileOutputStream(this.filenameService.getDictionaryTerms(this.name), true);
        } catch (IOException e) {
            throw new ApplicationException("The term database could not be opened");
        }
    }

    private void readDictionaryTerms() {
        try (FileInputStream fileInputStream = new FileInputStream(this.filenameService.getDictionaryTerms(this.name))) {
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
        try (FileInputStream fileInputStream = new FileInputStream(this.filenameService.getDictionaryIDF(this.name))) {
            Protobufs.DocumentFrequency documentFrequency = Protobufs.DocumentFrequency.parseFrom(fileInputStream);
            this.documents = documentFrequency.getDocuments();
            this.indexDocumentFrequency = new SparseVector(documentFrequency.getFrequency().getPosList(), documentFrequency.getFrequency().getValList());
            log.info("IDF read. Terms {}, Indexed documents: {}", this.indexDocumentFrequency.getSetTerms().size(), this.documents);
        } catch (FileNotFoundException e) {
            log.info("The document frequency was not found");
        } catch (IOException e) {
            throw new ApplicationException("Error reading the document frequency table");
        }
    }

    private synchronized void writeIndexDocumentFrequency() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.filenameService.getDictionaryIDF(this.name), false);
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

    @Override
    public synchronized void updateTermFrequency(SparseVector vector) {
        vector.getSetTerms()
                .forEach(index ->
                        this.indexDocumentFrequency.increment(index, 1));
        this.documents += 1;
        this.resetIdf();
        this.writeIndexDocumentFrequency();
    }

    @Override
    public synchronized float getIdf(Integer index) {
        return this.preCalculatedIdf
                .computeIfAbsent(index, idx -> (float) Math.log(this.documents / this.indexDocumentFrequency.get(idx)));
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
