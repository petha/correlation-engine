package Correlation.Extractor;

import Correlation.Model.Dictionary;
import Correlation.Model.Document;
import Correlation.Model.SparseVector;
import Correlation.Transformer.InputTransformer;
import Correlation.Transformer.NullTransformer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UniqWordsExtractor extends VectorExtractor {

    private Dictionary dictionary;
    private TokenizerME tokenizer;
    private SnowballStemmer snowballStemmer;
    private InputTransformer transformer;
    private Predicate<String> filter;

    public UniqWordsExtractor(String sourceField, InputTransformer transformer, Predicate<String> filter) {
        super(sourceField);

        this.transformer = transformer;
        this.filter = filter;
        InputStream modelStream = getClass().getResourceAsStream("/en-token.bin");
        TokenizerModel model;
        try {
            model = new TokenizerModel(modelStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read token model");
        }
        this.tokenizer = new TokenizerME(model);
        this.snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        this.dictionary = Dictionary.getInstance();
    }

    public UniqWordsExtractor(String sourceField) {
        this(sourceField, new NullTransformer(), (ignore) -> true);
    }

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
                .peek(keyword -> this.dictionary.add(keyword))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((term, frequency) ->
                        sparseVector.increment(this.dictionary.getIndex(term), Math.toIntExact(frequency)));
        return sparseVector;
    }

    @Override
    public void printStatistics() {
        System.out.println(
                String.format(
                        "UniqWordsExtractor: Dict: %s", this.dictionary));
    }
}