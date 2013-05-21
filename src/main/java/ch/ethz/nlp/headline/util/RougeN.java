package ch.ethz.nlp.headline.util;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

/**
 * Computes the ROUGE-N metric for summarization candidates.
 * 
 * CoreNLP lemmas serve as the basic unit. That is, {@link LemmaAnnotation}s
 * must be present on the given references and candidates.
 */
public class RougeN {

	private final int n;

	public RougeN(int n) {
		super();

		checkArgument(n > 0);
		this.n = n;
	}

	/**
	 * Computes the recall of a given candidate summary with respect to the
	 * given list of reference summaries.
	 * 
	 * @param references
	 *            list of reference summaries
	 * @param candidate
	 *            candidate summary
	 * @return the recall
	 */
	public double compute(List<CoreMap> references, CoreMap candidate) {
		double numerator = 0.0;
		double denominator = 0.0;
		Set<String> candidateNGrams = getNGrams(candidate);
		for (CoreMap reference : references) {
			Set<String> refNGrams = getNGrams(reference);
			numerator += Sets.intersection(refNGrams, candidateNGrams).size();
			denominator += refNGrams.size();
		}
		return numerator / denominator;
	}

	private Set<String> getNGrams(CoreMap sentence) {
		List<CoreLabel> allLabels = sentence.get(TokensAnnotation.class);
		Set<String> result = new HashSet<>();
		for (int i = 0; i < allLabels.size() - n + 1; i++) {
			List<CoreLabel> labels = allLabels.subList(i, i + n);
			List<String> lemmas = Lists.transform(labels, LEMMA_TO_STRING);
			String nGram = StringUtils.join(lemmas, " ");
			result.add(nGram);
		}
		return result;
	}

	private static final Function<CoreLabel, String> LEMMA_TO_STRING = new Function<CoreLabel, String>() {

		@Override
		public String apply(CoreLabel input) {
			return input.lemma();
		}

	};

}
