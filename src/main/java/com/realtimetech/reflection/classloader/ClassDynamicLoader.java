package com.realtimetech.reflection.classloader;

public class ClassDynamicLoader extends ClassLoader {
	public ClassDynamicLoader() {
		super(null);
	}

	public ClassDynamicLoader(Class<?> baseClass) {
		super(baseClass.getClassLoader());
	}

	public ClassDynamicLoader(ClassLoader classLoader) {
		super(classLoader);
	}

	public Class<?> loadClassByBytes(byte[] bytes, String className) {
		return defineClass(className, bytes, 0, bytes.length);
	}
}
