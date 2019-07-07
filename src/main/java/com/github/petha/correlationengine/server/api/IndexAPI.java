package com.github.petha.correlationengine.server.api;

import com.github.petha.correlationengine.server.CorrelationService;
import com.github.petha.correlationengine.server.dto.DocumentDTO;
import com.github.petha.correlationengine.server.dto.MatchDTO;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("index")
@AllArgsConstructor
@Validated
public class IndexAPI {

    private CorrelationService service;

    // TODO: add drop document and update document

    @PostMapping
    public UUID createDocument(@RequestBody @Valid DocumentDTO document) {
        return service.indexDocument(document);
    }

    @GetMapping("/correlate/{analyze}/{id}")
    public Stream<MatchDTO> getMatching(@PathVariable("analyze") @Pattern(regexp = "^[a-zA-Z0-9]{1,10}$") String analyze, @PathVariable UUID id) {
        return service.findMatches(id, analyze, 0.5);
    }
}
