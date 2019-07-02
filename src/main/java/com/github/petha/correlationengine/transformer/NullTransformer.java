package com.github.petha.correlationengine.transformer;

public class NullTransformer implements InputTransformer {
    @Override
    public String transform(String input) {
        return input;
    }
}
