package ch.ethz.nlp.headline;

import java.util.Objects;

public class DocumentId {

	private final String set;
	private final String name;

	public DocumentId(String set, String name) {
		super();
		this.set = set;
		this.name = name;
	}

	public String getSet() {
		return set;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, set);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		DocumentId other = (DocumentId) obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(set, other.set);
	}

	@Override
	public String toString() {
		return set + "/" + name;
	}

}
