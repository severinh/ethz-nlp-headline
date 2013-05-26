package ch.ethz.nlp.headline.learning;

import libsvm.svm_node;
import libsvm.svm_problem;

public class SVMProblem extends svm_problem {

	private static final long serialVersionUID = -4717491747803040309L;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < l; i++) {
			double label = y[i];
			svm_node[] nodes = x[i];

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
