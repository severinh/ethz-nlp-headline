package ch.ethz.nlp.headline;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.util.AnsiColor;
import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.util.NGramHitVisualizer;
import ch.ethz.nlp.headline.util.RougeN;
import edu.stanford.nlp.pipeline.Annotation;

public class EvaluationOutput {

	private static final Logger LOG = LoggerFactory
			.getLogger(EvaluationOutput.class);

	private static final double ROUGE_1_THRESHOLD = 0.20;
	private static final double ROUGE_2_THRESHOLD = 0.05;

	private static final AnsiColor MODEL_COLOR = AnsiColor.BLUE;

	public void log(Task task, Collection<Peer> peers) throws IOException {
		RougeN rouge1 = new RougeN(1);
		RougeN rouge2 = new RougeN(2);

		NGramHitVisualizer visualizer = NGramHitVisualizer.of(task.getModels());
		List<Annotation> modelAnnotations = visualizer.getModelAnnotations();

		for (Model model : task.getModels()) {
			String content = model.getContent();
			String logString = String.format("MODEL\t\t%s", content);
			LOG.info(MODEL_COLOR.makeString(logString));
		}

		for (Peer peer : peers) {
			String generatorId = peer.getGeneratorId();
			String headline = peer.load();

			Annotation annotation = getAnnotation(headline);
			String visualization = visualizer.visualize(annotation);

			double rouge1Recall = rouge1.compute(modelAnnotations, annotation);
			double rouge2Recall = rouge2.compute(modelAnnotations, annotation);
			String rouge1String = String.format("%.2f", rouge1Recall)
					.substring(1);
			String rouge2String = String.format("%.2f", rouge2Recall)
					.substring(1);

			if (!generatorId.equals("BASE")) {
				if (rouge1Recall < ROUGE_1_THRESHOLD) {
					rouge1String = AnsiColor.RED.makeString(rouge1String);
				}

				if (rouge2Recall < ROUGE_2_THRESHOLD) {
					rouge2String = AnsiColor.RED.makeString(rouge2String);
				}
			}

			LOG.info(String.format("%s\t%s %s\t%s", generatorId, rouge1String,
					rouge2String, visualization));
		}
	}

	private Annotation getAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		return annotation;
	}

}
