package ch.ethz.nlp.headline.duc;

import java.nio.file.Path;

import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.DocumentId;

public class DucDocument implements Document {

	private final Path path;
	private final DocumentId id;
	private final String content;

	public DucDocument(Path path, DocumentId id, String content) {
		this.path = path;
		this.id = id;
		this.content = content;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public DocumentId getId() {
		return id;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "Document [id=" + id + "]";
	}

}
