package org.ethz.nlp.headline;

import java.util.List;

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
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((document == null) ? 0 : document.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		return true;
	}

}
