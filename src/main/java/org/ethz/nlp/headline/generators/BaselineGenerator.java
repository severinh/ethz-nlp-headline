package org.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.Properties;

import org.ethz.nlp.headline.Document;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Generator that simply extracts the first sentence from the given text.
 */
public class BaselineGenerator implements Generator {

	private final StanfordCoreNLP pipeline;

	public BaselineGenerator() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
	}

	@Override
	public String getId() {
		return "BASE";
	}

	@Override
	public String generate(Document document) throws IOException {
		String content = document.load();
		Annotation annotation = new Annotation(content);
		pipeline.annotate(annotation);

		CoreMap sentenceMap = annotation.get(SentencesAnnotation.class).get(0);
		return sentenceMap.toString();
	}

}
