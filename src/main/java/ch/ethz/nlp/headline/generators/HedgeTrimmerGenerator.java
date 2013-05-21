package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.StringUtils;

public class HedgeTrimmerGenerator extends CoreNLPGenerator {

	private static final Logger LOG = LoggerFactory
			.getLogger(HedgeTrimmerGenerator.class);

	public HedgeTrimmerGenerator(Dataset dataset) throws IOException {

	}

	@Override
	public String getId() {
		return "HEDGE-TRIMMER";
	}

	@Override
	public String generate(Document document) throws IOException {
		Annotation annotation = getTokenizedSentenceDocumentAnnotation(document);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		CoreMap firstSentence = sentences.get(0);

		Annotation sentenceAnnotation = makeAnnotationFromSentence(firstSentence);

		getPosTagger().annotate(sentenceAnnotation);
		getLemmatizer().annotate(sentenceAnnotation);
		getParser().annotate(sentenceAnnotation);
		getNER().annotate(sentenceAnnotation);

		Tree tree = firstSentence.get(TreeAnnotation.class);

		// SemanticGraph dependencies = firstSentence
		// .get(CollapsedCCProcessedDependenciesAnnotation.class);

		Tree sTree = getLowestLeftmostSWithNPVP(tree);
		if (sTree == null) {
			// Account for cases where the first sentence is only a fragment
			sTree = tree;
		}

		sTree = removeLowContentNodes(sTree);

		String result = StringUtils.join(sTree.yieldWords(), " ");
		if (result.length() > MAX_LENGTH) {
			result = result.substring(0, MAX_LENGTH);
		}
		return result;
	}

	private Tree getLowestLeftmostSWithNPVP(Tree tree) {
		Tree result = null;
		if (tree.value().equals("S")) {
			boolean hasNPChild = false;
			boolean hasVPChild = false;
			for (Tree child : tree.children()) {
				if (child.value().equals("NP")) {
					hasNPChild = true;
				} else if (child.value().equals("VP")) {
					hasVPChild = true;
				}
				if (hasNPChild && hasVPChild) {
					break;
				}
			}
			if (hasNPChild && hasVPChild) {
				result = tree;
			}
		}
		// Only consider the left-most child
		Tree firstChild = tree.firstChild();
		if (firstChild != null) {
			Tree childResult = getLowestLeftmostSWithNPVP(firstChild);
			if (childResult != null) {
				result = childResult;
			}
		}
		return result;
	}

	private Tree removeLowContentNodes(Tree tree) {
		return tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			private Set<String> DETERMINERS = ImmutableSet.of("a", "the");

			@Override
			public boolean accept(Tree tree) {
				CoreLabel label = (CoreLabel) tree.label();
				String lemma = label.get(LemmaAnnotation.class);
				return !DETERMINERS.contains(lemma);
			}

		});
	}

}
