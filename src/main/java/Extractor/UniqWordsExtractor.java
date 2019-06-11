package Extractor;

import Model.Document;
import Transformer.InputTransformer;
import Transformer.NullTransformer;
import lombok.NonNull;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UniqWordsExtractor extends VectorExtractor {

    @NonNull
    private SortedSet<String> keywords;
    private TokenizerME tokenizer;
    private SnowballStemmer snowballStemmer;
    private InputTransformer transformer;
    private Predicate<String> filter;

    public UniqWordsExtractor(String sourceField, InputTransformer transformer, Predicate<String> filter) throws IOException {
        super(sourceField);

        this.transformer = transformer;
        this.filter = filter;
        InputStream modelStream = getClass().getResourceAsStream("/en-token.bin");
        TokenizerModel model = new TokenizerModel(modelStream);
        this.tokenizer = new TokenizerME(model);
        this.snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        this.keywords = new TreeSet<>();
    }

    public UniqWordsExtractor(String sourceField) throws IOException {
        this(sourceField, new NullTransformer(), (ignore) -> true);
    }

    @Override
    public Stream<Double> extract(Document document) {
        String content = this.getContent(document);
        String[] tokens = tokenizer.tokenize(content);

        Map<String, Long> amount = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .map(this.transformer::transform)
                .map(this.snowballStemmer::stem)
                .map(CharSequence::toString)
                .filter(this.filter)
                .peek(keyword -> this.keywords.add(keyword))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return keywords.parallelStream()
                .map(keyword -> (double) amount.getOrDefault(keyword, 0L));
    }
}
