package org.ethz.nlp.headline;

import java.util.List;
import java.util.Objects;

public class Task {

	private final Document document;
	private final List<Model> models;

	public Task(Document document, List<Model> models) {
		super();
		this.document = document;
		this.models = models;
	}

	public Document getDocument() {
		return document;
	}

	public List<Model> getModels() {
		return models;
	}

	@Override
	public String toString() {
		return "Task [document=" + document + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(document);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		Task other = (Task) obj;
		return Objects.equals(document, other.document);
	}

}
