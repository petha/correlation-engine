package Server.API;

import Server.CorrelationService;
import Server.DTO.AnalyzerDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public void createAnalyzer(@RequestBody AnalyzerDTO analyzer) throws Exception {
        this.service.registerAnalyzer(analyzer);
    }
}
