package com.realtimetech.reflection.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import com.realtimetech.reflection.classfile.file.ClassFile;

public class ClassDynamicLoader extends URLClassLoader {
	private HashMap<String, byte[]> classMap;

	public ClassDynamicLoader() {
		this(null);
	}

	public ClassDynamicLoader(ClassLoader classLoader) {
		super(new URL[0], classLoader);
		this.classMap = new HashMap<String, byte[]>();
	}

	public void addURL(URL url) {
		super.addURL(url);
	}
	
	public void addClass(String className, byte[] bytes) {
		this.classMap.put(className, bytes);
	}

	public void addClassFile(String className, byte[] bytes) {
		this.classMap.put(className, bytes);
	}

	public void addClassFile(ClassFile className) {
		this.classMap.put(className.getName(), className.getBytes());
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> result = null;
		try {
			result = super.findClass(name);
		} catch (ClassNotFoundException e) {
			if (this.classMap.containsKey(name)) {
				byte[] bytes = this.classMap.get(name);
				this.classMap.remove(name);
				result = defineClass(name, bytes, 0, bytes.length);
			}
		}

		if (result == null) {
			throw new ClassNotFoundException();
		}

		return result;
	}

	public Class<?> loadClassByBytes(String className, byte[] bytes) {
		return defineClass(className, bytes, 0, bytes.length);
	}
}
