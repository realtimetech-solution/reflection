package com.realtimetech.reflection.classfile.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.realtimetech.reflection.classfile.compiler.Message.MessageType;
import com.realtimetech.reflection.classloader.ClassDynamicLoader;

public class ClassCompiler {
	private File jdkDirectory;

	private HashMap<String, List<Source>> sourcesMap;

	private List<File> classpathFiles;

	private ClassLoader parentClassLoader;
	private ClassDynamicLoader classDynamicLoader;

	private ArrayList<Message> compileMessages;

	public ClassCompiler(File jdkDirectory) {
		this(ClassLoader.getSystemClassLoader(), jdkDirectory);
	}

	public ClassCompiler(ClassLoader parentClassLoader, File jdkDirectory) {
		this.parentClassLoader = parentClassLoader;

		this.jdkDirectory = jdkDirectory;
		this.sourcesMap = new HashMap<String, List<Source>>();
		this.classpathFiles = new LinkedList<File>();
		this.compileMessages = new ArrayList<Message>();
	}

	public ClassCompiler addSource(String packageName, Source source) {
		if (!this.sourcesMap.containsKey(packageName)) {
			this.sourcesMap.put(packageName, new LinkedList<Source>());
		}

		this.sourcesMap.get(packageName).add(source);

		return this;
	}

	public Source getSource(String packageName, String fileName) {
		if (this.sourcesMap.containsKey(packageName)) {
			List<Source> sources = this.sourcesMap.get(packageName);

			for (Source source : sources) {
				if (source.getFileName().equalsIgnoreCase(fileName)) {
					return source;
				}
			}
		}

		return null;
	}

	public ClassCompiler addClasspath(File classpath) {
		if (classpath.exists()) {
			String classpathName = classpath.getName();

			if (classpathName.endsWith(".jar") && classpathName.endsWith(".class") && classpathName.endsWith(".zip")) {
				this.classpathFiles.add(classpath);
			}
		}

		return this;
	}

	public List<File> getClasspathFiles() {
		return classpathFiles;
	}

	public File getJdkDirectory() {
		return jdkDirectory;
	}

	public ClassDynamicLoader getClassDynamicLoader() {
		return this.classDynamicLoader;
	}

	private void searchClassInDirectory(List<File> classFiles, File directory) {
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					searchClassInDirectory(classFiles, file);
				} else if (file.getName().endsWith(".class")) {
					classFiles.add(file);
				}
			}
		}
	}

	public boolean compile() throws IOException {
		System.setProperty("java.home", this.jdkDirectory.getAbsolutePath());

		File workingDirectory;

		do {
			workingDirectory = new File("build" + File.separator + UUID.randomUUID().toString() + File.separator);
		} while (workingDirectory.exists());

		List<File> sourceFiles = new LinkedList<File>();
		List<File> classFiles = new LinkedList<File>();

		for (String packageName : sourcesMap.keySet()) {
			List<Source> sources = sourcesMap.get(packageName);

			for (Source source : sources) {
				File packageFolder = new File(workingDirectory.getAbsolutePath() + File.separator + packageName.replace(".", File.separator) + File.separator);
				File sourceFile = new File(packageFolder.getPath() + File.separator + source.getFileName());
				packageFolder.mkdirs();

				Files.write(sourceFile.toPath(), source.getSourceCode().getBytes(StandardCharsets.UTF_8));

				sourceFiles.add(sourceFile);
			}
		}

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		String classPath = System.getProperty("java.class.path");

		for (File classpathFile : classpathFiles) {
			classPath += File.pathSeparator + classpathFile.getAbsolutePath();
		}

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		Iterable<String> options = Arrays.asList("-classpath", classPath);
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnit);

		boolean result = task.call();
		try {
			if (result) {
				this.searchClassInDirectory(classFiles, workingDirectory);
				this.classDynamicLoader = new ClassDynamicLoader(this.parentClassLoader);

				for (File classFile : classFiles) {
					String path = classFile.getAbsolutePath().replace(workingDirectory.getAbsolutePath(), "");
					String className = path.substring(1, path.lastIndexOf(".")).replace(File.separator, ".");

					this.classDynamicLoader.addClassFile(className, Files.readAllBytes(classFile.toPath()));
				}

				return true;
			} else {
				this.compileMessages.clear();
				for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
					MessageType type = null;

					switch (diagnostic.getKind()) {
					case ERROR:
						type = MessageType.ERROR;
						break;
					case MANDATORY_WARNING:
						type = MessageType.WARNING;
						break;
					case NOTE:
						type = MessageType.NOTE;
						break;
					case OTHER:
						type = MessageType.NOTE;
						break;
					case WARNING:
						type = MessageType.WARNING;
						break;
					}

					this.compileMessages.add(new Message(type, diagnostic.getLineNumber(), diagnostic.getMessage(null)));
				}

				return false;
			}

		} finally {
			for (File sourceFile : sourceFiles) {
				sourceFile.delete();
			}
		}
	}

	public ArrayList<Message> getCompileMessages() {
		return compileMessages;
	}

	public void printAllCompileMessages() {
		for (Message message : compileMessages) {
			System.out.println(message.toString());
		}
	}
}