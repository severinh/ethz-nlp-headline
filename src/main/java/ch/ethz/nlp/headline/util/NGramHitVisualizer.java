package ch.ethz.nlp.headline.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.Model;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

public class NGramHitVisualizer {

	private static final AnsiColor UNIGRAM_HIT_COLOR = AnsiColor.YELLOW;
	private static final AnsiColor BIGRAM_HIT_COLOR = AnsiColor.GREEN;

	private final List<Annotation> modelAnnotations;
	private final Set<String> modelUnigrams;
	private final Set<String> modelBigrams;

	public NGramHitVisualizer(List<Annotation> modelAnnotations) {
		this.modelAnnotations = ImmutableList.copyOf(modelAnnotations);
		this.modelUnigrams = new HashSet<>();
		this.modelBigrams = new HashSet<>();

		for (Annotation annotation : modelAnnotations) {
			CoreNLPUtil.ensureLemmaAnnotation(annotation);
			modelUnigrams.addAll(CoreNLPUtil.getNGrams(annotation, 1));
			modelBigrams.addAll(CoreNLPUtil.getNGrams(annotation, 2));
		}
	}

	public static NGramHitVisualizer of(List<Model> models) {
		List<Annotation> modelAnnotations = new ArrayList<>();
		for (Model model : models) {
			modelAnnotations.add(new Annotation(model.getContent()));
		}
		NGramHitVisualizer visualizer = new NGramHitVisualizer(modelAnnotations);
		return visualizer;
	}

	public List<Annotation> getModelAnnotations() {
		return modelAnnotations;
	}

	public String visualize(Annotation candidate) {
		CoreNLPUtil.ensureLemmaAnnotation(candidate);

		StringBuilder builder = new StringBuilder();
		List<CoreLabel> labels = candidate.get(TokensAnnotation.class);

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
				word = BIGRAM_HIT_COLOR.makeString(word);
			} else if (isInModelUnigram) {
				word = UNIGRAM_HIT_COLOR.makeString(word);
			}

			builder.append(word);
		}

		return builder.toString();
	}

	public String visualize(Document document) {
		return visualize(new Annotation(document.getContent()));
	}

}
