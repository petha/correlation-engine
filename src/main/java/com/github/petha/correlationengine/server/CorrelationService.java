package com.github.petha.correlationengine.server;

import com.github.petha.correlationengine.model.Analyzer;
import com.github.petha.correlationengine.CorrelationEngine;
import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.server.dto.AnalyzerDTO;
import com.github.petha.correlationengine.server.dto.DocumentDTO;
import com.github.petha.correlationengine.server.dto.MatchDTO;
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
    private CorrelationEngine engine = new CorrelationEngine();

    public UUID indexDocument(DocumentDTO dto) {
        Document document = dto.getDocument();
        engine.analyze(document);
        return document.getId();
    }

    public Stream<MatchDTO> findMatches(UUID id, String analyze, double cutOff) {
        return engine.correlate(id, analyze, cutOff).stream()
                .limit(40)
                .map(MatchDTO::fromCorrelation);
    }

    public List<String> getAnalyzers() {
        return engine.getAnalyzers().stream()
                .map(Analyzer::getName)
                .collect(Collectors.toList());
    }

    public void registerAnalyzer(AnalyzerDTO analyzerDTO) throws Exception {
        engine.addAnalyzer(analyzerDTO.getAnalyzer());
    }

    // TODO: add drop document and update document
}
