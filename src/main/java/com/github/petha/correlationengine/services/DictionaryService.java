package com.github.petha.correlationengine.services;

import com.github.petha.correlationengine.model.Dictionary;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@NoArgsConstructor
public class DictionaryService {
    private ConcurrentMap<String, Dictionary> dictionaries = new ConcurrentHashMap<>();

    public Dictionary getDictionary() {
        return this.dictionaries.computeIfAbsent("dictionary", Dictionary::new);
    }
}
