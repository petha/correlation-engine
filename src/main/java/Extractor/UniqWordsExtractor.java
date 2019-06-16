package Extractor;

import Model.Document;
import Transformer.InputTransformer;
import Transformer.NullTransformer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UniqWordsExtractor extends VectorExtractor {

    private LinkedHashSet<String> dictionary;
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
        this.dictionary = new LinkedHashSet<String>();
    }

    public UniqWordsExtractor(String sourceField) throws IOException {
        this(sourceField, new NullTransformer(), (ignore) -> true);
    }

    @Override
    public Stream<Integer> extract(Document document) {
        String content = this.getContent(document);
        String[] tokens = tokenizer.tokenize(content);

        Map<String, Long> amount = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .map(this.transformer::transform)
                .map(this.snowballStemmer::stem)
                .map(CharSequence::toString)
                .filter(this.filter)
                .peek(keyword -> this.dictionary.add(keyword))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return dictionary.parallelStream()
                .map(keyword -> amount.getOrDefault(keyword, 0L))
                .map(Math::toIntExact);
    }

    @Override
    public void printStatistics() {
        System.out.println(
                String.format(
                        "UniqWordsExtractor: Dict: %s", this.dictionary));
    }
}
