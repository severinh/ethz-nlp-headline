package ch.ethz.nlp.headline;

import java.util.ResourceBundle;

import com.google.common.base.Optional;

public class Config {

	private final Optional<String> filterDocumentId;

	public Config() {
		ResourceBundle bundle = ResourceBundle.getBundle("main");

		String documentId = bundle.getString("filter_document_id");
		if (documentId.isEmpty()) {
			filterDocumentId = Optional.absent();
		} else {
			filterDocumentId = Optional.of(documentId);
		}
	}

	public Optional<String> getFilterDocumentId() {
		return filterDocumentId;
	}

}
