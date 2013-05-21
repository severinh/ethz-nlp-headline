package ch.ethz.nlp.headline.duc2004;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.ethz.nlp.headline.DocumentId;
import ch.ethz.nlp.headline.Model;

public class Duc2004Model implements Model {

	private final Path path;
	private final DocumentId documentId;
	private final String id;
	private final String content;

	public Duc2004Model(Path path) throws IOException {
		String[] parts = path.getFileName().toString().split("\\.");
		String documentSet = (parts[0] + parts[3]).toLowerCase();
		String documentName = (parts[5] + "." + parts[6]);

		this.path = path;
		this.documentId = new DocumentId(documentSet, documentName);
		this.id = parts[4];
		this.content = new String(Files.readAllBytes(getPath())).trim();
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
