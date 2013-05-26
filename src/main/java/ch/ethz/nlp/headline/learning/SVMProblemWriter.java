package ch.ethz.nlp.headline.learning;

import libsvm.svm_node;
import libsvm.svm_problem;

public class SVMProblemWriter {

	public static String toString(svm_problem problem) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < problem.l; i++) {
			double label = problem.y[i];
			svm_node[] nodes = problem.x[i];

			builder.append(label);

			for (svm_node node : nodes) {
				builder.append(' ');
				builder.append(node.index);
				builder.append(':');
				builder.append(node.value);
			}

			builder.append('\n');
		}

		return builder.toString();
	}

}
