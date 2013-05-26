package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import libsvm.svm;
import libsvm.svm_model;
import ch.ethz.nlp.headline.cache.AnnotationProvider;
import ch.ethz.nlp.headline.compressor.TrainedCompressor;
import ch.ethz.nlp.headline.preprocessing.CombinedPreprocessor;
import ch.ethz.nlp.headline.selection.SentencesSelector;
import ch.ethz.nlp.headline.selection.TfIdfProvider;
import ch.ethz.nlp.headline.selection.ScoredSentencesSelector;
import ch.ethz.nlp.headline.util.GentleAnnotationStringBuilder;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.PriorityQueue;

public class TrainedGenerator extends CoreNLPGenerator {

	private final TfIdfProvider tfIdfProvider;
	private final SentencesSelector sentencesSelector;
	private final TrainedCompressor trainedCompressor;

	public TrainedGenerator(AnnotationProvider annotationProvider,
			TfIdfProvider tfIdfProvider) {
		super(annotationProvider, CombinedPreprocessor.all(),
				GentleAnnotationStringBuilder.INSTANCE);

		svm_model model = null;
		try {
			model = svm.svm_load_model("rouge.model");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		this.tfIdfProvider = tfIdfProvider;
		this.sentencesSelector = new ScoredSentencesSelector(tfIdfProvider);
		this.trainedCompressor = new TrainedCompressor(model);
	}

	@Override
	public String getId() {
		return "LEARN";
	}

	@Override
	protected Annotation generate(Annotation annotation) {
		PriorityQueue<String> tfIdfMap = tfIdfProvider.getTfIdfMap(annotation);

		annotation = sentencesSelector.select(annotation);
		annotation = trainedCompressor.compress(annotation, tfIdfMap);

		return annotation;
	}

}
