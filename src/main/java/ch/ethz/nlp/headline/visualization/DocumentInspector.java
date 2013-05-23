package ch.ethz.nlp.headline.visualization;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.nlp.headline.Dataset;
import ch.ethz.nlp.headline.Document;
import ch.ethz.nlp.headline.Task;
import ch.ethz.nlp.headline.duc2004.Duc2004Dataset;

import com.google.common.collect.ImmutableSet;

public class DocumentInspector {

	private static final Logger LOG = LoggerFactory
			.getLogger(DocumentInspector.class);

	public void inspect(Task task) {
		NGramHitVisualizer visualizer = NGramHitVisualizer.of(task.getModels());
		visualizer.setShowPerSentenceRecall(true);
		String result = visualizer.visualize(task.getDocument());
		String id = task.getDocument().getId().toString();
		LOG.info(String.format("Inspecting document %s\n%s", id, result));
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			throw new IllegalArgumentException(
					"specify at least one document id");
		}

		Set<String> selectedDocumentIds = ImmutableSet.copyOf(args);

		Dataset dataset = Duc2004Dataset.ofDefaultRoot();
		List<Task> tasks = dataset.getTasks();
		DocumentInspector inspector = new DocumentInspector();

		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			Document document = task.getDocument();
			String documentId = document.getId().toString();

			if (selectedDocumentIds.contains(documentId)) {
				inspector.inspect(task);
			}
		}
	}

}
