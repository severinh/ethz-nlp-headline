package ch.ethz.nlp.headline.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Computes the ROUGE-N metric for summarization candidates.
 * 
 * CoreNLP lemmas serve as the basic unit. That is, {@link LemmaAnnotation}s
 * must be present on the given references and candidates.
 */
public class RougeN {

	private final List<Set<String>> referencesNGrams;
	private final int denominator;
	private final int n;

	public RougeN(List<Annotation> references, int n) {
		super();

		checkArgument(n > 0);

		this.referencesNGrams = new ArrayList<>(references.size());
		this.n = n;

		int denominator = 0;
		for (Annotation reference : references) {
			Set<String> referenceNGrams = CoreNLPUtil.getNGrams(reference, n);
			referencesNGrams.add(referenceNGrams);
			denominator += referenceNGrams.size();
		}
		this.denominator = denominator;
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
	public double compute(Annotation candidate) {
		int num = 0;
		Set<String> candidateNGrams = CoreNLPUtil.getNGrams(candidate, n);
		for (Set<String> referenceNGrams : referencesNGrams) {
			num += Sets.intersection(referenceNGrams, candidateNGrams).size();
		}
		return (double) num / (double) denominator;
	}

	public double compute(CoreLabel candidateLabel) {
		if (n > 1) {
			return 0;
		}

		String candidateLemma = candidateLabel.lemma();
		int num = 0;
		for (Set<String> referenceNGram : referencesNGrams) {
			if (referenceNGram.contains(candidateLemma)) {
				num++;
			}
		}
		return (double) num / (double) denominator;
	}

}
