package Server.API;

import Server.CorrelationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void createAnalyzer() {
        this.service.registerAnalyzer();
    }
}
