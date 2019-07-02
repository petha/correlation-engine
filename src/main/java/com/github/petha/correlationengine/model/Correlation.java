package com.github.petha.correlationengine.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Correlation {
    private UUID sourceId;
    private UUID targetId;
    private double score;
}
