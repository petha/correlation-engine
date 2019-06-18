package Server;

import Correlation.Model.Analyzer;
import Correlation.CorrelationEngine;
import Correlation.Model.Document;
import Server.DTO.AnalyzerDTO;
import Server.DTO.DocumentDTO;
import Server.DTO.MatchDTO;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service()
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CorrelationService {
    private CorrelationEngine ENGINE = new CorrelationEngine();

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

    public void registerAnalyzer(AnalyzerDTO analyzerDTO) {
        ENGINE.addAnalyzer(analyzerDTO.getAnalyzer());
    }

    // TODO: add drop document and update document
}
