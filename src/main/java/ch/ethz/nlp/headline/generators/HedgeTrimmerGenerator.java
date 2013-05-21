package ch.ethz.nlp.headline.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;

import com.google.common.collect.ImmutableSet;

import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.parser.tools.PunctEquivalenceClasser;
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
		return "HEDGE";
	}

	@Override
	public String generate(Document document) {
		Annotation annotation = getTokenizedSentenceDocumentAnnotation(document);
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		CoreMap firstSentence = sentences.get(0);

		Annotation sentenceAnnotation = makeAnnotationFromSentence(firstSentence);

		getPosTagger().annotate(sentenceAnnotation);
		getLemmatizer().annotate(sentenceAnnotation);
		getNER().annotate(sentenceAnnotation);
		getParser().annotate(sentenceAnnotation);

		Tree tree = firstSentence.get(TreeAnnotation.class);

		// SemanticGraph dependencies = firstSentence
		// .get(CollapsedCCProcessedDependenciesAnnotation.class);

		Tree sTree = getLowestLeftmostSWithNPVP(tree);
		if (sTree == null) {
			// Account for cases where the first sentence is only a fragment
			sTree = tree;
		}

		sTree = removeLowContentNodes(sTree);
		sTree = shortenIterativelyRule1(sTree);
		sTree = shortenIterativelyRule2(sTree);

		String result = treeToString(sTree, MAX_LENGTH);
		getStatistics().addSummaryResult(result);
		return truncate(result);
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
		tree = tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			private Set<String> DETERMINERS = ImmutableSet.of("a", "the");

			@Override
			public boolean accept(Tree tree) {
				CoreLabel label = (CoreLabel) tree.label();
				String lemma = label.lemma();
				return !DETERMINERS.contains(lemma);
			}

		});

		tree = tree.prune(new Filter<Tree>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(Tree tree) {
				// Remove [PP … [NNP [X] …] …] where X is a tagged as part of a
				// time expression
				// Currently disabled because it trims too much in some cases
				if (false && tree.value().equals("PP")) {
					Stack<Tree> treeStack = new Stack<>();
					treeStack.addAll(Arrays.asList(tree.children()));
					while (!treeStack.isEmpty()) {
						Tree child = treeStack.pop();
						if (!child.value().equals("PP")) {
							CoreLabel childLabel = (CoreLabel) child.label();
							String ner = childLabel.ner();
							if (Objects.equals(ner, "DATE")) {
								logTrimming(tree, "Date");
								return false;
							} else {
								treeStack.addAll(Arrays.asList(child.children()));
							}
						}
					}
				}
				// Remove [NNP [X]] where X is a tagged as part of a
				// time expression
				if (tree.value().equals("NNP")) {
					CoreLabel childLabel = (CoreLabel) tree.firstChild()
							.label();
					String ner = childLabel.ner();
					if (Objects.equals(ner, "DATE")) {
						logTrimming(tree, "Date");
						return false;
					}
				}

				return true;
			}

		});

		return tree;
	}

	private Tree shortenIterativelyRule1(Tree fullTree) {
		List<CoreLabel> candidates = new ArrayList<>();
		Set<String> XP = ImmutableSet.of("NP", "VP", "S");

		for (Tree tree : fullTree) {
			if (XP.contains(tree.value())) {
				Tree[] children = tree.children();
				if (children[0].value().equals(tree.value())) {
					for (int i = 1; i < children.length; i++) {
						candidates.add((CoreLabel) children[i].label());
					}
				}
			}
		}

		if (!candidates.isEmpty()) {
			// Just eliminate the right-most and deepest candidate for the
			// moment
			final CoreLabel candidate = candidates.get(candidates.size() - 1);
			fullTree = fullTree.prune(new Filter<Tree>() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean accept(Tree tree) {
					CoreLabel label = (CoreLabel) tree.label();
					if (label != null
							&& Objects.equals(
									label.get(BeginIndexAnnotation.class),
									candidate.get(BeginIndexAnnotation.class))
							&& Objects.equals(
									label.get(EndIndexAnnotation.class),
									candidate.get(EndIndexAnnotation.class))) {
						logTrimming(tree, "Iterative Shortening Rule 1");
						return false;
					}
					return true;
				}

			});
		}

		return fullTree;
	}

	private String treeToString(Tree tree, int maxLength) {
		StringBuilder builder = new StringBuilder();
		for (Label label : tree.yield()) {
			CoreLabel coreLabel = (CoreLabel) label;
			String word = coreLabel.word();
			if (!PunctEquivalenceClasser.getPunctClass(word).isEmpty()) {
				continue;
			}
			if (builder.length() + word.length() > maxLength) {
				break;
			}
			builder.append(word);
			builder.append(" ");
		}
		String result = builder.toString();
		return result;
	}

	private Tree shortenIterativelyRule2(Tree tree) {
		if (tree.value().equals("S")) {
			while (tree.firstChild() != null
					&& !tree.firstChild().value().equals("NP")) {
				logTrimming(tree.firstChild(), "Iterative Shortening Rule 2");
				tree.removeChild(0);
			}
		}
		return tree;
	}

	private void logTrimming(Tree trimmedTree, String rule) {
		String trimmedText = StringUtils.join(trimmedTree.yieldWords(), " ");
		LOG.debug("Trimming '" + trimmedText + "' [" + rule + "]");
	}

}
