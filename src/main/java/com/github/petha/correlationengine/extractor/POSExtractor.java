package com.github.petha.correlationengine.extractor;

import com.github.petha.correlationengine.model.Document;
import com.github.petha.correlationengine.model.SparseVector;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class POSExtractor extends VectorExtractor {

    private POSTaggerME tagger;
    private String sourceField;

    public POSExtractor(String source) throws IOException {
        super(source);
        InputStream modelStream = getClass().getResourceAsStream("/en-pos-maxent.bin");
        POSModel model = new POSModel(modelStream);
        this.tagger = new POSTaggerME(model);
        this.sourceField = source;
    }

    @Override
    public SparseVector extract(Document document) {

        String[] whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
                .tokenize(document.getFields().get(this.sourceField));

        Map<String, Long> tagging = Arrays.stream(
                this.tagger.tag(whitespaceTokenizerLine))
                .collect(
                        Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()));

        //return Arrays.stream(this.tagger.getAllPosTags())
        //        .map(tag -> tagging.getOrDefault(tag, 0L))
        //        .map(Math::toIntExact);
        return null;
    }
}
