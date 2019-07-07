package com.github.petha.correlationengine.extractor;

import com.github.petha.correlationengine.exceptions.ApplicationException;
import com.github.petha.correlationengine.model.Dictionary;
import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.model.SparseVector;
import com.github.petha.correlationengine.transformer.InputTransformer;
import com.github.petha.correlationengine.transformer.NullTransformer;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class UniqWordsExtractor extends VectorExtractor {

    private Dictionary dictionary;
    private TokenizerME tokenizer;
    private SnowballStemmer snowballStemmer;
    private InputTransformer transformer;
    private Predicate<String> filter;

    public UniqWordsExtractor(String sourceField, InputTransformer transformer, Predicate<String> filter, Dictionary dictionary) {
        super(sourceField);

        this.transformer = transformer;
        this.filter = filter;
        InputStream modelStream = getClass().getResourceAsStream("/en-token.bin");
        TokenizerModel model;
        try {
            model = new TokenizerModel(modelStream);
        } catch (IOException e) {
            throw new ApplicationException("Cannot read token model");
        }
        this.tokenizer = new TokenizerME(model);
        this.snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        this.dictionary = dictionary;
    }

    public UniqWordsExtractor(String sourceField, Dictionary dictionary) {
        this(sourceField, new NullTransformer(), ignore -> true, dictionary);
    }

    @SuppressWarnings("squid:S3864")
    @Override
    public SparseVector extract(Document document) {
        String content = this.getContent(document);
        String[] tokens = tokenizer.tokenize(content);
        SparseVector sparseVector = new SparseVector();

        Arrays.stream(tokens)
                .map(String::toLowerCase)
                .map(this.transformer::transform)
                .map(this.snowballStemmer::stem)
                .map(CharSequence::toString)
                .filter(this.filter)
                .peek(this.dictionary::add)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((term, frequency) ->
                        sparseVector.increment(this.dictionary.getIndex(term), Math.toIntExact(frequency)));
        return sparseVector;
    }

    @Override
    public void printStatistics() {
        log.info("UniqWordsExtractor: Dict: {}", this.dictionary);
    }
}
