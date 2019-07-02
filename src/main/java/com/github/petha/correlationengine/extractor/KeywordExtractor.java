package com.github.petha.correlationengine.extractor;

import com.github.petha.correlationengine.model.Dictionary;
import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.model.SparseVector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class KeywordExtractor extends VectorExtractor {

    @NonNull
    private Set<String> keywords;
    private TokenizerME tokenizer;
    private SnowballStemmer snowballStemmer;
    private Dictionary dictionary;

    public KeywordExtractor(String sourceField, Set<String> keywords) throws IOException {
        super(sourceField);
        InputStream modelStream = getClass().getResourceAsStream("/en-token.bin");
        TokenizerModel model = new TokenizerModel(modelStream);
        this.tokenizer = new TokenizerME(model);
        this.snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        this.keywords = keywords.stream()
                .map(String::toLowerCase)
                .map(snowballStemmer::stem)
                .map(CharSequence::toString)
                .collect(Collectors.toSet());

        this.dictionary = Dictionary.getInstance();
    }

    @SuppressWarnings("squid:S3864")
    @Override
    public SparseVector extract(Document document) {
        String content = this.getContent(document);
        String[] tokens = tokenizer.tokenize(content);
        SparseVector sparseVector = new SparseVector();

        Arrays.stream(tokens)
                .map(String::toLowerCase)
                .map(this.snowballStemmer::stem)
                .map(CharSequence::toString)
                .filter(this.keywords::contains)
                .peek(this.dictionary::add)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((term, frequency) ->
                        sparseVector.increment(this.dictionary.getIndex(term), Math.toIntExact(frequency)));
        return sparseVector;
    }

    @Override
    public void printStatistics() {
        log.info("KeywordExtractor: Dict: {}", this.keywords);
    }
}
