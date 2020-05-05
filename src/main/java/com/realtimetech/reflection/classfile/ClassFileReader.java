package com.realtimetech.reflection.classfile;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.realtimetech.reflection.classfile.file.ClassFile;
import com.realtimetech.reflection.classfile.file.ClassFileStream;
import com.realtimetech.reflection.classfile.file.ClassFileStreamSet;

public class ClassFileReader {
	public enum ClassType {
		JAR, FOLDER
	}
	
	private static ClassType getClassType(Class<?> clazz) {
		String rootFolderPath = getClassRootPath(clazz);
		if (rootFolderPath != null) {
			File rootFolder = new File(rootFolderPath);
			File classFile = new File(rootFolder.getPath() + "/" + getClassPath(clazz));

			if (classFile.exists()) {
				return ClassType.FOLDER;
			}

			return ClassType.JAR;
		}

		return null;
	}

	private static ClassFileStreamSet getClassFilesFromFolder(Class<?> clazz) throws FileNotFoundException {
		ClassFileStreamSet classFileStreamSet = new ClassFileStreamSet();

		if (clazz.getSimpleName().isEmpty())
			return classFileStreamSet;

		String rootFolderPath = getClassRootPath(clazz);
		if (rootFolderPath != null) {
			File rootFolder = new File(rootFolderPath);
			File classFile = new File(rootFolder.getPath() + "/" + getClassPath(clazz));

			List<File> allClassList = new ArrayList<File>();

			if (classFile.exists()) {
				File folder = classFile.getParentFile();
				allClassList.add(classFile);
				if (folder.isDirectory()) {
					File[] listFiles = folder.listFiles();

					for (File listFile : listFiles) {
						if (listFile.getName().startsWith(clazz.getSimpleName() + "$")) {
							allClassList.add(listFile);
						}
					}
				}
			}
			for (File file : allClassList) {
				if (file.exists()) {
					InputStream inputStream = new FileInputStream(file);
					String className = clazz.getName() + ".class";
					className = className.replace(clazz.getSimpleName() + ".class", file.getName()).replace(".class",
							"");
					classFileStreamSet.getCloseableList().add(inputStream);
					classFileStreamSet.getClassFileStreamList()
							.add(new ClassFileStream(inputStream, file.length(), className));
				}
			}
		}
		return classFileStreamSet;
	}

	private static ClassFileStreamSet getClassFilesFromJar(Class<?> clazz) throws IOException {
		ClassFileStreamSet classFileStreamSet = new ClassFileStreamSet();

		if (clazz.getSimpleName().isEmpty())
			return classFileStreamSet;

		JarFile jarFile = new JarFile(getClassRootPath(clazz));
		List<JarEntry> jarEntrys = Collections.list(jarFile.entries());

		List<ZipEntry> allClassList = new ArrayList<ZipEntry>();
		allClassList.add(jarFile.getEntry(getClassPath(clazz)));
		for (JarEntry jarEntry : jarEntrys) {
			if (!jarEntry.isDirectory()) {
				if (jarEntry.getName().startsWith(clazz.getName().replace(".", "/") + "$")
						&& jarEntry.getName().toLowerCase().endsWith(".class")) {
					allClassList.add(jarEntry);
				}
			}
		}

		for (ZipEntry zipEntry : allClassList) {
			InputStream inputStream = jarFile.getInputStream(zipEntry);

			String className = zipEntry.getName().substring(0, zipEntry.getName().length() - 6);
			className = className.replace('/', '.');

			classFileStreamSet.getCloseableList().add(inputStream);
			classFileStreamSet.getClassFileStreamList()
					.add(new ClassFileStream(inputStream, zipEntry.getSize(), className));
		}

		classFileStreamSet.getCloseableList().add(jarFile);

		return classFileStreamSet;
	}

	private static String getClassRootPath(Class<?> clazz) {
		try {
			return clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private static String getClassPath(Class<?> clazz) {
		try {
			return clazz.getName().replace(".", "/") + ".class";
		} catch (Exception e) {
			return null;
		}
	}

	public static ClassFile[] getClassBytes(Class<?> clazz) throws IOException {
		ClassType classType = getClassType(clazz);

		if (classType == null)
			return new ClassFile[] {};

		ClassFileStreamSet classFileStreamSet = null;

		List<ClassFile> classFileList = new ArrayList<ClassFile>();

		switch (classType) {
		case FOLDER:
			classFileStreamSet = getClassFilesFromFolder(clazz);
			break;
		case JAR:
			classFileStreamSet = getClassFilesFromJar(clazz);
			break;
		}

		for (ClassFileStream classFileStream : classFileStreamSet.getClassFileStreamList()) {
			ClassFile readClassFile = getClassFile(classFileStream.getInputStream(), (int) classFileStream.getSize(),
					classFileStream.getClassName());

			if (readClassFile != null) {
				classFileList.add(readClassFile);
			}

		}

		for (Closeable closeable : classFileStreamSet.getCloseableList()) {
			try {
				closeable.close();
			} catch (Exception e) {
			}
		}

		return classFileList.toArray(new ClassFile[classFileList.size()]);
	}

	private static byte[] readAllInputStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int readBytes;
		byte[] data = new byte[16384];

		while ((readBytes = inputStream.read(data, 0, data.length)) != -1) {
			byteArrayOutputStream.write(data, 0, readBytes);
		}

		return byteArrayOutputStream.toByteArray();
	}

	private static ClassFile getClassFile(InputStream inputStream, int size, String className) {
		try {
			return new ClassFile(className, ClassFileReader.readAllInputStream(inputStream));
		} catch (IOException e) {
			return null;
		}
	}

}
