package com.github.petha.correlationengine.server;

import com.github.petha.correlationengine.CorrelationEngine;
import com.github.petha.correlationengine.exceptions.ApplicationException;
import com.github.petha.correlationengine.model.Analyzer;
import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.server.dto.AnalyzerDTO;
import com.github.petha.correlationengine.server.dto.DocumentDTO;
import com.github.petha.correlationengine.server.dto.MatchDTO;
import com.github.petha.correlationengine.services.DictionaryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CorrelationService {
    private CorrelationEngine engine;
    private DictionaryService dictionaryService;

    public UUID indexDocument(DocumentDTO dto) {
        Document document = dto.getDocument();
        this.engine.analyze(document);
        return document.getId();
    }

    public Stream<MatchDTO> findMatches(UUID id, String analyze, double cutOff) {
        return this.engine.getAnalyzers().stream()
                .filter(analyzer -> analyzer.getName().equals(analyze))
                .findFirst()
                .map(analyzer -> this.engine.correlate(id, analyzer.getName(), cutOff).stream()
                        .limit(40)
                        .map(MatchDTO::fromCorrelation))
                .orElseThrow(() -> new ApplicationException("Analyzer does not exist"));
    }

    public List<String> getAnalyzers() {
        return this.engine.getAnalyzers().stream()
                .map(Analyzer::getName)
                .collect(Collectors.toList());
    }

    public void registerAnalyzer(AnalyzerDTO analyzerDTO) {
        this.engine
                .addAnalyzer(
                        analyzerDTO.getAnalyzer(this.dictionaryService.getDictionary(analyzerDTO.getName())));
    }

    // TODO: add drop document and update document
}
