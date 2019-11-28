package com.realtimetech.reflection.classfile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.stream.Stream;

public class ClassFinder {
	public static Class<?>[] getClassInPackages(Class<?> packageInClass) throws IOException {
		return getClassInPackages(packageInClass.getPackageName());
	}

	public static Class<?>[] getClassInPackages(String... packageNames) throws IOException {
		LinkedList<Class<?>> resultClasses = new LinkedList<Class<?>>();

		for (String packageName : packageNames) {
			String packagePath = packageName.replace('.', '/');
			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);

			while (resources.hasMoreElements()) {
				try {
					URL resource = resources.nextElement();
					URI packageUri = resource.toURI();

					Path root;

					if (packageUri.toString().startsWith("jar:")) {
						try {
							root = FileSystems.getFileSystem(packageUri).getPath(packagePath);
						} catch (final FileSystemNotFoundException e) {
							root = FileSystems.newFileSystem(packageUri, Collections.emptyMap()).getPath(packagePath);
						}
					} else {
						root = Paths.get(packageUri);
					}

					final String extension = ".class";
					try (final Stream<Path> allPaths = Files.walk(root)) {
						allPaths.filter(Files::isRegularFile).forEach(file -> {
							try {
								final String path = file.toString().replace('/', '.');
								final String name = path.substring(path.indexOf(packageName), path.length() - extension.length());
								resultClasses.add(Class.forName(name));
							} catch (final ClassNotFoundException | StringIndexOutOfBoundsException ignored) {
							}
						});
					}
				} catch (URISyntaxException e) {
				}
			}
		}

		return resultClasses.toArray(new Class<?>[resultClasses.size()]);
	}
}
