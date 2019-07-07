package com.github.petha.correlationengine.services;

import com.github.petha.correlationengine.exceptions.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
//@AllArgsConstructor
public class FilenameService {
    @Value("${correlation.database.path}")
    private String basePath;

    public String getVectorFile(String analyzer) {
        this.ensureDirectory(analyzer);
        return Paths.get(this.basePath, analyzer, "document.vec")
                .toString();
    }

    public String getDictionaryTerms(String analyzer) {
        this.ensureDirectory(analyzer);
        return Paths.get(this.basePath, analyzer, "dictionary.dic")
                .toString();
    }

    public String getDictionaryIDF(String analyzer) {
        this.ensureDirectory(analyzer);
        return Paths.get(this.basePath, analyzer, "dictionary.idf")
                .toString();
    }

    public String getDatabase() {
        this.ensureDirectory(null);
        return Paths.get(this.basePath, "main.db")
                .toString();
    }

    private void ensureDirectory(String analyzer) {
        Paths.get(this.basePath).toFile().mkdirs();

        if (analyzer != null) {
            if (analyzer.matches("^[a-zA-Z0-9]{1,10}$")) {
                Paths.get(this.basePath, analyzer).toFile().mkdirs();
            } else {
                throw new ApplicationException("Invalid path");
            }
        }
    }
}
