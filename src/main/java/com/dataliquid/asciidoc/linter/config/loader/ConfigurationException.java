package com.dataliquid.asciidoc.linter.config.loader;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}