package com.realtimetech.reflection.classfile.file;

public class ClassFile {
	private String name;
	private byte[] bytes;

	public ClassFile(String name, byte[] bytes) {
		this.name = name;
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getName() {
		return name;
	}
}