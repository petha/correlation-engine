package com.github.petha.correlationengine.transformer;

public class NullTransformer extends InputTransformer {
    @Override
    public String transform(String input) {
        return input;
    }
}
