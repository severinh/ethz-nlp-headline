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

public class Main {

	private static final String DUC_2004_ROOT = "duc2004";
	private static final String EVALUATION_CONFIG_FILENAME = "evaluation.conf";

	public static void main(String[] args) {
		Path datasetRoot = FileSystems.getDefault().getPath(DUC_2004_ROOT);
		Dataset dataset = new Duc2004Dataset(datasetRoot);
		List<Task> tasks = dataset.getTasks();
		List<Generator> generators = new ArrayList<>();
		generators.add(new BaselineGenerator());
		Map<Task, List<Peer>> peersMap = new HashMap<>();
		for (Task task : tasks) {
			try {
				List<Peer> peers = new ArrayList<>();
				for (Generator generator : generators) {
					String content = task.getDocument().load();
					String headline = generator.generate(content);
					Peer peer = dataset.makePeer(task, generator.getId());
					peer.store(headline);
					peers.add(peer);
				}
				peersMap.put(task, peers);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		EvaluationConfig config = new EvaluationConfig(dataset);
		Path configPath = FileSystems.getDefault().getPath(
				EVALUATION_CONFIG_FILENAME);
		config.write(configPath, peersMap);
	}
}
