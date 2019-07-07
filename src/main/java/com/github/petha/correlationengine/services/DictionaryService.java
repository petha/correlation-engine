package com.github.petha.correlationengine.services;

import com.github.petha.correlationengine.model.Dictionary;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private ConcurrentMap<String, Dictionary> dictionaries = new ConcurrentHashMap<>();
    @NonNull
    private FilenameService filenameService;

    public Dictionary getDictionary(String name) {
        return this.dictionaries.computeIfAbsent(name, n -> new Dictionary(n, filenameService));
    }
}
