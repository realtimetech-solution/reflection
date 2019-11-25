package com.realtimetech.reflection.classfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class ClassFinder {
	private static void recursiveSearch(File directory, String packageName, List<Class<?>> resultClasses) throws ClassNotFoundException {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					recursiveSearch(file, packageName + "." + file.getName(), resultClasses);
				} else if (file.getName().endsWith(".class")) {
					resultClasses.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				}
			}
		}
	}

	public static Class<?>[] getClassInPackages(Class<?> packageInClass) throws IOException {
		return getClassInPackages(packageInClass.getPackageName());
	}

	public static Class<?>[] getClassInPackages(String... packageNames) throws IOException {
		LinkedList<Class<?>> resultClasses = new LinkedList<Class<?>>();

		for(String packageName : packageNames) {
			String path = packageName.replace('.', '/');
			Enumeration<URL> resources = ClassFinder.class.getClassLoader().getResources(path);
			List<File> directories = new LinkedList<File>();

			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				directories.add(new File(resource.getFile()));
			}

			for (File directory : directories) {
				try {
					recursiveSearch(directory, packageName, resultClasses);
				} catch (ClassNotFoundException e) {
				}
			}
		}

		return resultClasses.toArray(new Class<?>[resultClasses.size()]);
	}
}
