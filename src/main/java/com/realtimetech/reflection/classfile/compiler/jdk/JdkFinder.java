package com.realtimetech.reflection.classfile.compiler.jdk;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class JdkFinder {
	public static File getDirectory(String version) {
		String operatingSystem = System.getProperty("os.name").toLowerCase();

		String[] folders = null;
		String fileName = null;

		if (operatingSystem.indexOf("mac") >= 0 || operatingSystem.indexOf("mac") >= 0) {
			folders = new String[] { "." + File.separator, File.separator + "Library" + File.separator + "Java" + File.separator + "JavaVirtualMachines", File.separator + "System" + File.separator + "Library" + File.separator + "Frameworks", File.separator + "usr" + File.separator + "libexec" + File.separator + "java_home" };
			fileName = "javac";
		} else if (operatingSystem.indexOf("win") >= 0) {
			folders = new String[] { "." + File.separator, System.getenv("ProgramFiles") + File.separator + "ojdkbuild", System.getenv("ProgramFiles(X86)") + File.separator + "ojdkbuild", System.getenv("ProgramFiles") + File.separator + "Java", System.getenv("ProgramFiles(X86)") + File.separator + "Java" };
			fileName = "javac.exe";
		} else if (operatingSystem.indexOf("nux") >= 0) {
			folders = new String[] { "." + File.separator, File.separator + "usr" + File.separator + "java", File.separator + "usr" + File.separator + "ojdkbuild", File.separator + "usr" + File.separator + "openjdk" };
			fileName = "javac";
		}

		File lastDirectory = null;

		for (String folder : folders) {
			File file = new File(folder);

			File[] searchFilesInDirectory = searchFilesInDirectory(file, fileName);

			if (searchFilesInDirectory.length > 0) {
				for (File jdkDirecotory : searchFilesInDirectory) {
					String absolutePath = jdkDirecotory.getAbsolutePath();
					if (absolutePath.contains(version)) {
						return jdkDirecotory.getParentFile().getParentFile();
					} else {
						lastDirectory = jdkDirecotory.getParentFile().getParentFile();
					}
				}
			}
		}

		return lastDirectory;
	}

	private static File[] searchFilesInDirectory(File folder, String fileName) {
		ArrayList<File> searchedFiles = new ArrayList<File>();
		try {
			Files.walkFileTree(Paths.get(folder.getPath()), new HashSet<FileVisitOption>(Arrays.asList(FileVisitOption.FOLLOW_LINKS)), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.getFileName().toString().contains(fileName)) {
						searchedFiles.add(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return searchedFiles.toArray(new File[searchedFiles.size()]);
	}

}
