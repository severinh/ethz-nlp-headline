package ch.ethz.nlp.headline.duc;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;

import ch.ethz.nlp.headline.Document;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class Duc2003Dataset extends DucDataset {

	private static final String ROOT = "duc2003";

	private static final Set<String> DOCUMENT_BLACKLIST = ImmutableSet
			.of("XIE19980503.0148");

	protected Duc2003Dataset(Path documentRoot, Path modelRoot, Path peerRoot) {
		super(documentRoot, modelRoot, peerRoot);
	}

	public static Duc2003Dataset ofRoot(Path root) {
		Path documentRoot = root.resolve("docs.without.headlines");
		Path evalRoot = root.resolve("detagged.duc2003.abstracts");
		Path modelRoot = evalRoot.resolve("models");
		Path peerRoot = evalRoot.resolve("peer0.1");

		return new Duc2003Dataset(documentRoot, modelRoot, peerRoot);
	}

	public static Duc2003Dataset ofDefaultRoot() {
		Path datasetRoot = FileSystems.getDefault().getPath(ROOT);

		return ofRoot(datasetRoot);
	}

	protected Optional<Document> makeDocument(Path documentPath)
			throws IOException {
		String documentName = documentPath.getFileName().toString();
		if (DOCUMENT_BLACKLIST.contains(documentName)) {
			return Optional.absent();
		}
		return super.makeDocument(documentPath);
	}

	@Override
	protected String getDocumentContent(Path documentPath) throws IOException {
		String content = super.getDocumentContent(documentPath);
		content = content.replaceAll("</?P>", "");
		content = content.replaceAll("</?ANNOTATION>", "");
		content = content.replaceAll("\\s+", " ");
		return content.trim();
	}

}
