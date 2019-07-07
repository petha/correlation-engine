package com.github.petha.correlationengine.server.api;

import com.github.petha.correlationengine.server.CorrelationService;
import com.github.petha.correlationengine.server.dto.AnalyzerDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("analyze")
@AllArgsConstructor
public class AnalyzeAPI {
    private CorrelationService service;

    @GetMapping
    public List<String> listAnalyzers() {
        return service.getAnalyzers();
    }

    @PostMapping
    public void createAnalyzer(@Valid @RequestBody AnalyzerDTO analyzer) {
        this.service.registerAnalyzer(analyzer);
    }
}
