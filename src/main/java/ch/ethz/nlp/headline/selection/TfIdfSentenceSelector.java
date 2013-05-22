package ch.ethz.nlp.headline.selection;

import java.util.List;
import edu.stanford.nlp.util.CoreMap;

public class TfIdfSentenceSelector extends SentencesSelector {

	private final TfIdfProvider tfIdfProvider;

	public TfIdfSentenceSelector(TfIdfProvider tfIdfProvider) {
		this.tfIdfProvider = tfIdfProvider;
	}

	@Override
	protected List<CoreMap> select(List<CoreMap> sentences) {
		// TODO Auto-generated method stub
		return null;
	}

}
