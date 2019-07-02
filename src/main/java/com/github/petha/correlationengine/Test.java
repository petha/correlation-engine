package com.github.petha.correlationengine;

import com.github.petha.correlationengine.extractor.UniqWordsExtractor;
import com.github.petha.correlationengine.model.Analyzer;
import com.github.petha.correlationengine.model.Document;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Test {
    private static final Map<UUID, String> data = new HashMap<>();

    public static Document getContent(String url) throws TikaException, IOException {
        Tika tika = new Tika();
        String content = tika.parseToString(new URL(url));

        return Document.builder()
                .fields(Map.of("description", content))
                .id(UUID.randomUUID())
                .build();
    }

    public static void main(String[] args) throws Exception {
        CorrelationEngine correlationEngine = new CorrelationEngine();


        correlationEngine.addAnalyzer(Analyzer.builder()
                .extractorList(List.of(new UniqWordsExtractor("description")))
                .name("UniqWords")
                .build()
        );

        Test test = new Test();
        for (int i = 0; i < 500; i++) {
            test.getCSV().forEach(correlationEngine::analyze);
        }
        correlationEngine.printStatistics();
        correlationEngine.shutdown();

    }

    private List<Document> getCSV() throws IOException {
        InputStream reviews = getClass().getResourceAsStream("/Restaurant_Reviews.tsv");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(reviews));
        List<Document> documents = new ArrayList<>();
        String nextLine = bufferedReader.readLine();

        while (nextLine != null) {
            // nextLine[] is an array of values from the line
            Document document = Document.builder()
                    .fields(Map.of("description", nextLine))
                    .id(UUID.randomUUID())
                    .build();
            documents.add(document);
            Test.data.put(document.getId(), document.getFields().get("description"));
            nextLine = bufferedReader.readLine();
        }

        return documents;
    }
}
