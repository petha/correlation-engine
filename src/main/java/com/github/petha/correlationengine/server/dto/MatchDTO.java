package com.github.petha.correlationengine.server.dto;

import com.github.petha.correlationengine.model.Correlation;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MatchDTO {
    private UUID documentId;
    private double score;

    public static MatchDTO fromCorrelation(Correlation correlation) {
        return MatchDTO.builder()
                .documentId(correlation.getTargetId())
                .score(correlation.getScore()).build();
    }
}
