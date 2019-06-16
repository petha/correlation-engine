package Server.API;

import Server.CorrelationService;
import Server.DTO.DocumentDTO;
import Server.DTO.MatchDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("index")
@AllArgsConstructor
public class IndexAPI {

    private CorrelationService service;

    // TODO: add drop document and update document

    @PostMapping
    public UUID createDocument(@RequestBody @Valid DocumentDTO document) {
        return service.indexDocument(document);
    }

    @GetMapping("/correlate/{analyze}/{id}")
    public Stream<MatchDTO> getMatching(@PathVariable String analyze, @PathVariable UUID id) {
        return service.findMatches(id, analyze, 0.5);
    }
}
