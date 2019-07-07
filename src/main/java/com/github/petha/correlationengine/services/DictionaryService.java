package com.github.petha.correlationengine.services;

import com.github.petha.correlationengine.model.Dictionary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DictionaryService {
    public Dictionary getDictionary() {
        return new Dictionary("dictionary");
    }
}
