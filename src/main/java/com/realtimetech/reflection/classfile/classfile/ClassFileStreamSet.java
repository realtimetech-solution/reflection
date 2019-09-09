package com.realtimetech.reflection.classfile.classfile;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class ClassFileStreamSet {
	private List<ClassFileStream> classFileStreamList;
	private List<Closeable> closeableList;

	public ClassFileStreamSet() {
		classFileStreamList = new ArrayList<ClassFileStream>();
		closeableList = new ArrayList<Closeable>();
	}

	public List<ClassFileStream> getClassFileStreamList() {
		return classFileStreamList;
	}

	public List<Closeable> getCloseableList() {
		return closeableList;
	}
}
