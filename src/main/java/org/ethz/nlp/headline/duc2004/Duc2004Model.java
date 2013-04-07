package org.ethz.nlp.headline.duc2004;

import java.nio.file.Path;

import org.ethz.nlp.headline.DocumentId;
import org.ethz.nlp.headline.Model;

public class Duc2004Model implements Model {

	private final Path path;
	private final DocumentId documentId;
	private final String id;

	public Duc2004Model(Path path) {
		String[] parts = path.getFileName().toString().split("\\.");
		String documentSet = (parts[0] + parts[3]).toLowerCase();
		String documentName = (parts[5] + "." + parts[6]);

		this.path = path;
		this.documentId = new DocumentId(documentSet, documentName);
		this.id = parts[4];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.IModel#getRelativePath()
	 */
	@Override
	public Path getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ethz.nlp.headline.IModel#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public String toString() {
		return "Model [documentId=" + documentId + ", id=" + id + "]";
	}

}
