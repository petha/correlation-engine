import Extractor.KeywordExtractor;
import Extractor.POSExtractor;
import Extractor.UniqWordsExtractor;
import Model.Document;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    public static void main(String[] args) throws IOException, TikaException {
        CorrelationEngine correlationEngine = new CorrelationEngine();

        correlationEngine.addAnalyzer(
                Analyzer.builder()
                        .extractorList(List.of(new POSExtractor("description")))
                        .name("AuthorAnalyzer")
                        .build());


        correlationEngine.addAnalyzer(Analyzer.builder()
                .extractorList(List.of(new UniqWordsExtractor("description")))
                .name("UniqWords")
                .build()
        );

        correlationEngine.addAnalyzer(
                Analyzer.builder()
                        .extractorList(
                                List.of(
                                        new KeywordExtractor("description", Set.of(
                                                "worst",
                                                "bad",
                                                "good"
                                        ))
                                ))
                        .name("Keywords")
                        .build());

        Test test = new Test();
        test.getCSV().forEach(correlationEngine::analyze);

        correlationEngine.printIndicesStatistics();
        correlationEngine.correlate(
                correlationEngine.getIndex("UniqWords").stream().findFirst().get(), 0.3
        ).collect(Collectors.toList())
                .forEach(correlation -> System.out.println(
                        String.format("Correlation: %f\n%s\n%s\n\n",
                                correlation.getScore(),
                                Test.data.get(correlation.getSourceId()),
                                Test.data.get(correlation.getTargetId()))));

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
