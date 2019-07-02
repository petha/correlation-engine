package com.github.petha.correlationengine.extractor;

import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.model.SparseVector;
import lombok.NonNull;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KeywordExtractor extends VectorExtractor {

    @NonNull
    private Set<String> keywords;
    private TokenizerME tokenizer;
    private SnowballStemmer snowballStemmer;

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
    }

    @Override
    public SparseVector extract(Document document) {
        String content = this.getContent(document);
        String[] tokens = tokenizer.tokenize(content);

        Map<String, Long> amount = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .map(this.snowballStemmer::stem)
                .map(CharSequence::toString)
                .filter(this.keywords::contains)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return null;
        //return keywords.parallelStream()
        //       .sorted()
        //       .map(keyword -> amount.getOrDefault(keyword, 0L))
        //       .map(Math::toIntExact);
    }

    @Override
    public void printStatistics() {
        System.out.println(
                String.format(
                        "KeywordExtractor: Dict: %s", this.keywords));
    }
}
