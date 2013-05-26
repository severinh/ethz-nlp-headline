package ch.ethz.nlp.headline.learning;

import java.util.ArrayList;
import java.util.List;

import libsvm.svm_node;

public class SVMProblemBuilder {

	private final List<Double> labels = new ArrayList<>(15000);
	private final List<svm_node[]> nodeMatrix = new ArrayList<>(15000);

	public void addExample(svm_node[] nodes, double label) {
		labels.add(label);
		nodeMatrix.add(nodes);
	}

	public SVMProblem build() {
		SVMProblem problem = new SVMProblem();
		problem.l = labels.size();
		problem.y = new double[problem.l];
		problem.x = new svm_node[problem.l][];
		for (int i = 0; i < problem.l; i++) {
			problem.y[i] = labels.get(i);
			problem.x[i] = nodeMatrix.get(i);
		}

		return problem;
	}

}
