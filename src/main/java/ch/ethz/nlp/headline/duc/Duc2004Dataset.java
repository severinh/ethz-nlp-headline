package ch.ethz.nlp.headline.duc;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Duc2004Dataset extends DucDataset {

	private static final String ROOT = "duc2004";

	protected Duc2004Dataset(Path documentRoot, Path modelRoot, Path peerRoot) {
		super(documentRoot, modelRoot, peerRoot);
	}

	public static Duc2004Dataset ofRoot(Path root) {
		Path documentRoot = root.resolve("docs");
		Path modelRoot = root.resolve("eval").resolve("models").resolve("1");
		Path peerRoot = root.resolve("eval").resolve("peers").resolve("1");

		return new Duc2004Dataset(documentRoot, modelRoot, peerRoot);
	}

	public static Duc2004Dataset ofDefaultRoot() {
		Path datasetRoot = FileSystems.getDefault().getPath(ROOT);
		Duc2004Dataset dataset = ofRoot(datasetRoot);
		return dataset;
	}

}
