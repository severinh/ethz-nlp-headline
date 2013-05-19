package ch.ethz.nlp.headline;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import ch.ethz.nlp.headline.duc2004.Duc2004Dataset;
import ch.ethz.nlp.headline.generators.BaselineGenerator;
import ch.ethz.nlp.headline.generators.Generator;
import ch.ethz.nlp.headline.generators.PosFilteredGenerator;
import ch.ethz.nlp.headline.generators.TfIdfWordsGenerator;
import ch.ethz.nlp.headline.generators.TfIdfSentenceGenerator;

public class Main {

	private static final String DUC_2004_ROOT = "duc2004";
	private static final String EVALUATION_CONFIG_FILENAME = "evaluation.conf";

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {
		Path datasetRoot = FileSystems.getDefault().getPath(DUC_2004_ROOT);
		Dataset dataset = new Duc2004Dataset(datasetRoot);
		List<Task> tasks = dataset.getTasks();

		List<Generator> generators = new ArrayList<>();
		// generators.add(new BaselineGenerator(dataset));
		// generators.add(new PosFilteredGenerator(dataset));
		// generators.add(new TfIdfWordsGenerator(dataset));
		generators.add(new TfIdfSentenceGenerator(dataset));

		Multimap<Task, Peer> peersMap = LinkedListMultimap.create();
		for (Task task : tasks) {
			Document document = task.getDocument();
			for (Generator generator : generators) {
				String headline = generator.generate(document);
				Peer peer = dataset.makePeer(task, generator.getId());
				try {
					peer.store(headline);
				} catch (IOException e) {
					System.exit(-1);
					e.printStackTrace();
				}
				peersMap.put(task, peer);
			}
		}

		EvaluationConfig config = new EvaluationConfig(dataset);
		Path configPath = FileSystems.getDefault().getPath(
				EVALUATION_CONFIG_FILENAME);
		config.write(configPath, peersMap);
	}
}
