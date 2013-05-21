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
import ch.ethz.nlp.headline.generators.CombinedSentenceGenerator;
import ch.ethz.nlp.headline.generators.Generator;
import ch.ethz.nlp.headline.generators.PosFilteredGenerator;
import ch.ethz.nlp.headline.generators.TfIdfWordsGenerator;

public class Main {

	private static final String EVALUATION_CONFIG_FILENAME = "evaluation.conf";

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {
		Dataset dataset = Duc2004Dataset.ofDefaultRoot();
		List<Task> tasks = dataset.getTasks();

		List<Generator> generators = new ArrayList<>();
		generators.add(new BaselineGenerator(dataset));
		generators.add(new PosFilteredGenerator(dataset));
		generators.add(new TfIdfWordsGenerator(dataset));
		generators.add(new CombinedSentenceGenerator(dataset));

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
