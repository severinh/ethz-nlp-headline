package ch.ethz.nlp.headline;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.util.CoreNLPUtil;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

public class EvaluationOutput {

	private static final Logger LOG = LoggerFactory
			.getLogger(EvaluationOutput.class);

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
		Set<String> modelUnigrams = new HashSet<>();
		Set<String> modelBigrams = new HashSet<>();

		for (Model model : task.getModels()) {
			String modelContent = model.getContent();
			Annotation annotation = getAnnotation(modelContent);

			modelUnigrams.addAll(CoreNLPUtil.getNGrams(annotation, 1));
			modelBigrams.addAll(CoreNLPUtil.getNGrams(annotation, 2));

			String logString = String.format("MODEL\t%s", modelContent);
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
				if (!word.equals("'s")) {
					builder.append(" ");
				}
				if (isInModelBigram) {
					word = bigramHitColor.makeString(word);
				} else if (isInModelUnigram) {
					word = unigramHitColor.makeString(word);
				}

				builder.append(word);

			}
			LOG.info(String.format("%s\t%s", generatorId, builder.toString()));
		}
	}

	private Annotation getAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		return annotation;
	}

}
