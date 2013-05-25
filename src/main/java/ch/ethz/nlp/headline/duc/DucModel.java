package ch.ethz.nlp.headline.duc;

import java.io.IOException;
import java.nio.file.Path;

import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.Model;

public class DucModel implements Model {

	private final Path path;
	private final DocumentId documentId;
	private final String id;
	private final String content;

	public DucModel(Path path, DocumentId documentId, String id, String content)
			throws IOException {
		this.path = path;
		this.documentId = documentId;
		this.id = id;
		this.content = content;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public String getId() {
		return id;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "Model [documentId=" + documentId + ", id=" + id + "]";
	}

}
