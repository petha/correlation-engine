package Server;

import Correlation.Analyzer;
import Correlation.CorrelationEngine;
import Correlation.Extractor.UniqWordsExtractor;
import Correlation.Model.Document;
import Server.DTO.DocumentDTO;
import Server.DTO.MatchDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service()
public class CorrelationService {
    private static CorrelationEngine ENGINE = new CorrelationEngine();

    public UUID indexDocument(DocumentDTO dto) {
        Document document = dto.getDocument();
        ENGINE.analyze(document);
        return document.getId();
    }

    public Stream<MatchDTO> findMatches(UUID id, String analyze, double cutOff) {
        return ENGINE.correlate(id, analyze, cutOff).map(MatchDTO::fromCorrelation);
    }

    public List<String> getAnalyzers() {
        return ENGINE.getAnalyzers().stream()
                .map(Analyzer::getName)
                .collect(Collectors.toList());
    }

    public void registerAnalyzer() {
        try {
            ENGINE.addAnalyzer(Analyzer.builder()
                    .extractorList(List.of(new UniqWordsExtractor("description")))
                    .name("UniqWords")
                    .build());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: add drop document and update document
}
