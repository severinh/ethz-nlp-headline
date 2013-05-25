package ch.ethz.nlp.headline.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Computes the ROUGE-N metric for summarization candidates.
 * 
 * CoreNLP lemmas serve as the basic unit. That is, {@link LemmaAnnotation}s
 * must be present on the given references and candidates.
 */
public class RougeN {

	private final List<Annotation> references;
	private final int n;

	public RougeN(List<Annotation> references, int n) {
		super();

		checkArgument(n > 0);

		this.references = references;
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
	public double compute(Annotation candidate) {
		double numerator = 0.0;
		double denominator = 0.0;
		Set<String> candidateNGrams = CoreNLPUtil.getNGrams(candidate, n);
		for (Annotation reference : references) {
			Set<String> refNGrams = CoreNLPUtil.getNGrams(reference, n);
			numerator += Sets.intersection(refNGrams, candidateNGrams).size();
			denominator += refNGrams.size();
		}
		return numerator / denominator;
	}

}
