package org.ethz.nlp.headline;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ethz.nlp.headline.duc2004.Duc2004Dataset;
import org.ethz.nlp.headline.generators.BaselineGenerator;
import org.ethz.nlp.headline.generators.Generator;
import org.ethz.nlp.headline.generators.PosFilteredGenerator;

public class Main {

	private static final String DUC_2004_ROOT = "duc2004";
	private static final String EVALUATION_CONFIG_FILENAME = "evaluation.conf";

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {
		Path datasetRoot = FileSystems.getDefault().getPath(DUC_2004_ROOT);
		Dataset dataset = new Duc2004Dataset(datasetRoot);
		List<Task> tasks = dataset.getTasks();

		List<Generator> generators = new ArrayList<>();
		generators.add(new BaselineGenerator());
		generators.add(new PosFilteredGenerator());

		Map<Task, List<Peer>> peersMap = new HashMap<>();
		for (Task task : tasks) {
			String content = task.getDocument().load();
			List<Peer> peers = new ArrayList<>();
			for (Generator generator : generators) {
				String headline = generator.generate(content);
				Peer peer = dataset.makePeer(task, generator.getId());
				try {
					peer.store(headline);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				peers.add(peer);
			}
			peersMap.put(task, peers);
		}

		EvaluationConfig config = new EvaluationConfig(dataset);
		Path configPath = FileSystems.getDefault().getPath(
				EVALUATION_CONFIG_FILENAME);
		config.write(configPath, peersMap);
	}
}
