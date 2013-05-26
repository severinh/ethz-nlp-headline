package ch.ethz.nlp.headline.learning;

import libsvm.svm_parameter;

public class SVMParameter extends svm_parameter {

	private static final long serialVersionUID = -7146212446747869229L;

	public static SVMParameter makeDefault() {
		SVMParameter parameter = new SVMParameter();
		parameter.svm_type = EPSILON_SVR;
		parameter.kernel_type = RBF;
		parameter.degree = 3;
		parameter.gamma = 0.0625;
		parameter.coef0 = 0;
		parameter.nu = 0.125;
		parameter.cache_size = 40;
		parameter.C = 8;
		parameter.eps = 1e-3;
		parameter.p = 0.1;
		parameter.shrinking = 1;
		parameter.probability = 0;
		parameter.nr_weight = 0;
		parameter.weight_label = new int[0];
		parameter.weight = new double[0];

		return parameter;
	}

}
