package ch.ethz.nlp.headline.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.nlp.headline.util.CoreNLPUtil;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.util.CoreMap;

public class AnnotationCache implements AnnotationProvider {

	private static final String DEFAULT_ROOT = "cache";
	private static final int DEFAULT_NUM_PARSED_SENTENCES = 1;

	private final Path rootFolder;
	private final int numParsedSentences;
	private final MessageDigest messageDigest;
	private final Kryo kryo;

	public AnnotationCache(Path rootFolder, int numParsedSentences) {
		super();
		this.rootFolder = rootFolder;
		this.numParsedSentences = numParsedSentences;

		rootFolder.toFile().mkdirs();

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		this.messageDigest = messageDigest;
		this.kryo = new Kryo();

		kryo.register(LabeledScoredTreeNode.class,
				new FieldSerializer<LabeledScoredTreeNode>(kryo,
						LabeledScoredTreeNode.class));
		kryo.register(TreeGraphNode.class, new FieldSerializer<TreeGraphNode>(
				kryo, TreeGraphNode.class));
		kryo.register(EnglishGrammaticalStructure.class,
				new FieldSerializer<EnglishGrammaticalStructure>(kryo,
						EnglishGrammaticalStructure.class));
	}

	public AnnotationCache() {
		this(FileSystems.getDefault().getPath(DEFAULT_ROOT),
				DEFAULT_NUM_PARSED_SENTENCES);
	}

	@Override
	public Annotation getAnnotation(String content) {
		String hash = stringToMD5(content);
		Path cacheFilePath = rootFolder.resolve(hash);
		Annotation annotation = null;

		if (cacheFilePath.toFile().exists()) {
			annotation = loadAnnotation(cacheFilePath);
		} else {
			annotation = buildAnnotation(content);
			storeAnnotation(cacheFilePath, annotation);
		}

		return annotation;
	}

	private Annotation loadAnnotation(Path cacheFilePath) {
		Annotation annotation = null;
		File cacheFile = cacheFilePath.toFile();
		try (FileInputStream fileIn = new FileInputStream(cacheFile)) {
			try (Input in = new Input(fileIn)) {
				annotation = kryo.readObject(in, Annotation.class);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return annotation;
	}

	private Annotation buildAnnotation(String content) {
		Annotation annotation = new Annotation(content);
		CoreNLPUtil.ensureLemmaAnnotation(annotation);
		CoreNLPUtil.ensureNamedEntityTagAnnotation(annotation);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		int numSentences = Math.min(numParsedSentences, sentences.size());
		List<CoreMap> sentencesToParse = new ArrayList<>(sentences.subList(0,
				numSentences));

		Annotation annotationToParse = CoreNLPUtil
				.sentencesToAnnotation(sentencesToParse);
		CoreNLPUtil.ensureTreeAnnotation(annotationToParse);

		return annotation;
	}

	private void storeAnnotation(Path cacheFilePath, Annotation annotation) {
		File cacheFile = cacheFilePath.toFile();
		try (FileOutputStream fileOut = new FileOutputStream(cacheFile)) {
			try (Output output = new Output(fileOut)) {
				kryo.writeObject(output, annotation);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String stringToMD5(String content) {
		byte[] bytes = content.getBytes(Charset.forName("UTF-8"));
		messageDigest.update(bytes, 0, content.length());
		return new BigInteger(1, messageDigest.digest()).toString(16);
	}

}
