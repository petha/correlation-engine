package com.github.petha.correlationengine.services;

import com.github.petha.correlationengine.exceptions.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class FilenameService {
    private static final String INVALID_PATH = "Invalid path";

    @Value("${correlation.database.path}")
    private String basePath;

    public String getVectorFile(String analyzer) {
        this.ensureDirectory(analyzer);
        if (this.checkFilenameCorrect(analyzer)) {
            return Paths.get(this.basePath, analyzer, "document.vec")
                    .toString();
        }
        throw new ApplicationException(INVALID_PATH);
    }


    public String getDictionaryTerms(String analyzer) {
        this.ensureDirectory(analyzer);
        if (this.checkFilenameCorrect(analyzer)) {
            return Paths.get(this.basePath, analyzer, "dictionary.dic")
                    .toString();
        }
        throw new ApplicationException("Invalid path");
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

    private boolean checkFilenameCorrect(String analyzer) {
        return analyzer.matches("^[a-zA-Z0-9]+$");
    }

    private void ensureDirectory(String analyzer) {
        Paths.get(this.basePath).toFile().mkdirs();

        if (analyzer != null && checkFilenameCorrect(analyzer)) {
            Paths.get(this.basePath, analyzer).toFile().mkdirs();
        }
    }
}

