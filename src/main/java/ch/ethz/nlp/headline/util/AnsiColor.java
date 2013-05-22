package ch.ethz.nlp.headline.util;

public enum AnsiColor {

	RESET("\u001B[0m"), BLACK("\u001B[30m"), RED("\u001B[31m"), GREEN(
			"\u001B[32m"), YELLOW("\u001B[33m"), BLUE("\u001B[34m"), PURPLE(
			"\u001B[35m"), CYAN("\u001B[36m"), WHITE("\u001B[37m");

	private final String code;

	private AnsiColor(String code) {
		this.code = code;
	}

	public String makeString(String content) {
		return code + content + RESET.code;
	}

}