package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.StringUtils;

public abstract class CoreNLPGenerator implements Generator {

	private final StanfordCoreNLP pipeline;

	public CoreNLPGenerator(Dataset dataset, String... annotators)
			throws IOException {
		List<String> allAnnotators = new ArrayList<>();
		allAnnotators.add("tokenize");
		allAnnotators.addAll(Arrays.asList(annotators));

		Properties props = new Properties();
		props.put("annotators", StringUtils.join(allAnnotators, ","));
		pipeline = new StanfordCoreNLP(props);
	}
	
	public Annotation getDocumentAnnotation(Document document) throws IOException {
		String content = document.load();
		Annotation annotation = new Annotation(content);
		pipeline.annotate(annotation);
		return annotation;
	}
}
