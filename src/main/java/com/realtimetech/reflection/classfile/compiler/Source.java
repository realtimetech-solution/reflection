package com.realtimetech.reflection.classfile.compiler;

public class Source {
	private String fileName;

	private String sourceCode;

	public Source(String fileName, String sourceCode) {
		this.fileName = fileName;
		this.sourceCode = sourceCode;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
}
