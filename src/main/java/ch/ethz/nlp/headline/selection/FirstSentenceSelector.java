package ch.ethz.nlp.headline.selection;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.util.CoreMap;

public class FirstSentenceSelector extends SentencesSelector {

	@Override
	protected List<CoreMap> select(List<CoreMap> sentences) {
		return ImmutableList.of(sentences.get(0));
	}

}
