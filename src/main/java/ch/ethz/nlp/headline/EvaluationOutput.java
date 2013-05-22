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
	private final AnsiColor peerHitColor;

	public EvaluationOutput(AnsiColor modelColor, AnsiColor peerHitColor) {
		super();
		this.modelColor = modelColor;
		this.peerHitColor = peerHitColor;
	}

	public EvaluationOutput() {
		this(AnsiColor.BLUE, AnsiColor.GREEN);
	}

	public void log(Task task, Collection<Peer> peers) throws IOException {
		List<Model> models = task.getModels();
		Set<String> modelLemmas = new HashSet<>();

		for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
			Model model = models.get(modelIndex);
			String content = model.getContent();
			modelLemmas.addAll(getLemmas(getLabels(content)));

			String logString = String.format("MDL %d\t%s", modelIndex, content);
			LOG.info(modelColor.makeString(logString));
		}

		for (Peer peer : peers) {
			String generatorId = peer.getGeneratorId();
			String headline = peer.load();

			List<CoreLabel> labels = getLabels(headline);
			StringBuilder builder = new StringBuilder();
			for (CoreLabel label : labels) {
				boolean isHit = modelLemmas.contains(label.lemma());
				String word = label.word();
				if (!word.equals("'s")) {
					builder.append(" ");
				}
				if (isHit) {
					word = peerHitColor.makeString(word);
				}
				builder.append(word);
			}
			LOG.info(String.format("%s\t%s", generatorId, builder.toString()));
		}
	}

	private List<CoreLabel> getLabels(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.getTokenizer().annotate(annotation);
		CoreNLPUtil.getSentenceSplitter().annotate(annotation);
		CoreNLPUtil.getPosTagger().annotate(annotation);
		CoreNLPUtil.getLemmatizer().annotate(annotation);

		return annotation.get(TokensAnnotation.class);
	}

	private Set<String> getLemmas(List<CoreLabel> labels) {
		Set<String> lemmas = new HashSet<>();
		for (CoreLabel label : labels) {
			lemmas.add(label.lemma());
		}
		return lemmas;
	}

}
