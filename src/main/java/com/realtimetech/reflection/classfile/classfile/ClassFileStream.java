package com.realtimetech.reflection.classfile.classfile;

import java.io.InputStream;

public class ClassFileStream {
	private InputStream inputStream;
	private long size;
	private String className;

	public ClassFileStream(InputStream inputStream, long size, String className) {
		this.inputStream = inputStream;
		this.size = size;
		this.className = className;
	}

	public long getSize() {
		return size;
	}

	public String getClassName() {
		return className;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
}