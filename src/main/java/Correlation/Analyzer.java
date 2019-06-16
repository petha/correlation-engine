package Correlation;

import Correlation.Model.*;
import Correlation.Extractor.VectorExtractor;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Analyzer {
    @NonNull
    @Builder.Default
    private List<VectorExtractor> extractorList = new ArrayList<>();

    @NonNull
    private String name;

    IndexRecord analyze(final Document document) {
        List<Integer> vector = this.extractorList.parallelStream()
                .flatMap(extractor -> extractor.extract(document))
                .collect(Collectors.toList());

        return IndexRecord.builder()
                .id(document.getId())
                .name(this.getName())
                .vector(vector)
                .build();
    }

    void printStatistics() {
        extractorList.forEach(VectorExtractor::printStatistics);
    }
}
