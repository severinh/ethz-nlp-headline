package ch.ethz.nlp.headline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import ch.ethz.nlp.headline.util.RougeN;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

public class EvaluationOutput {

	private static final Logger LOG = LoggerFactory
			.getLogger(EvaluationOutput.class);

	private static final double ROUGE_1_THRESHOLD = 0.20;
	private static final double ROUGE_2_THRESHOLD = 0.05;

	public enum AnsiColor {
		RESET("\u001B[0m"), BLACK("\u001B[30m"), RED("\u001B[31m"), GREEN(
				"\u001B[32m"), YELLOW("\u001B[33m"), BLUE("\u001B[34m"), PURPLE(
				"\u001B[35m"), CYAN("\u001B[36m"), WHITE("\u001B[37m");

		private final String code;

		private AnsiColor(String code) {
			this.code = code;
		}

		public String makeString(String content) {
			return code + content + RESET.code;
		}

	}

	private final AnsiColor modelColor;
	private final AnsiColor unigramHitColor;
	private final AnsiColor bigramHitColor;

	public EvaluationOutput(AnsiColor modelColor, AnsiColor unigramHitColor,
			AnsiColor bigramHitColor) {
		super();
		this.modelColor = modelColor;
		this.unigramHitColor = unigramHitColor;
		this.bigramHitColor = bigramHitColor;
	}

	public EvaluationOutput() {
		this(AnsiColor.BLUE, AnsiColor.YELLOW, AnsiColor.GREEN);
	}

	public void log(Task task, Collection<Peer> peers) throws IOException {
		List<Annotation> modelAnnotations = new ArrayList<>();
		Set<String> modelUnigrams = new HashSet<>();
		Set<String> modelBigrams = new HashSet<>();
		RougeN rouge1 = new RougeN(1);
		RougeN rouge2 = new RougeN(2);

		for (Model model : task.getModels()) {
			String modelContent = model.getContent();
			Annotation annotation = getAnnotation(modelContent);

			modelAnnotations.add(annotation);
			modelUnigrams.addAll(CoreNLPUtil.getNGrams(annotation, 1));
			modelBigrams.addAll(CoreNLPUtil.getNGrams(annotation, 2));

			String logString = String.format("MODEL\t\t%s", modelContent);
			LOG.info(modelColor.makeString(logString));
		}

		for (Peer peer : peers) {
			String generatorId = peer.getGeneratorId();
			String headline = peer.load();

			Annotation annotation = getAnnotation(headline);
			StringBuilder builder = new StringBuilder();
			List<CoreLabel> labels = annotation.get(TokensAnnotation.class);

			for (int i = 0; i < labels.size(); i++) {
				boolean isInModelUnigram = false;
				boolean isInModelBigram = false;

				CoreLabel label = labels.get(i);
				String lemma = label.lemma();

				if (modelUnigrams.contains(lemma)) {
					isInModelUnigram = true;

					if (i > 0
							&& modelBigrams.contains(labels.get(i - 1).lemma()
									+ " " + lemma)) {
						isInModelBigram = true;
					}

					if (i < labels.size() - 1
							&& modelBigrams.contains(lemma + " "
									+ labels.get(i + 1).lemma())) {
						isInModelBigram = true;
					}
				}

				String word = label.word();
				if (i != 0 && !word.equals("'s")) {
					builder.append(" ");
				}

				if (isInModelBigram) {
					word = bigramHitColor.makeString(word);
				} else if (isInModelUnigram) {
					word = unigramHitColor.makeString(word);
				}

				builder.append(word);
			}

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
					rouge2String, builder.toString()));
		}
	}

	private Annotation getAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		return annotation;
	}

}
