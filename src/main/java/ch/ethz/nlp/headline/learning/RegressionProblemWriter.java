package ch.ethz.nlp.headline.learning;

import java.util.Map.Entry;

import edu.berkeley.compbio.jlibsvm.regression.MutableRegressionProblemImpl;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;

public class RegressionProblemWriter {

	public static String toString(
			MutableRegressionProblemImpl<SparseVector> problem) {
		StringBuilder builder = new StringBuilder();

		for (Entry<SparseVector, Float> entry : problem.examples.entrySet()) {
			SparseVector vector = entry.getKey();
			float target = entry.getValue();

			builder.append(Float.toString(target));
			builder.append(' ');
			builder.append(vector.toString());
			builder.append('\n');
		}

		return builder.toString();
	}

}
