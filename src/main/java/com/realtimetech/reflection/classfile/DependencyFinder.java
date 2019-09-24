package com.realtimetech.reflection.classfile;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.realtimetech.reflection.classfile.classfile.ClassFile;
import com.realtimetech.reflection.classloader.ClassDynamicLoader;

public class DependencyFinder {
	private static void recursiveSearch(Class<?> clazz, ClassDynamicLoader classDynamicLoader,
			List<Class<?>> resultClasses) throws IOException {

		ClassFile[] classFiles = ClassFileReader.getClassBytes(clazz);

		List<String> classNames = new LinkedList<String>();

		for (ClassFile classFile : classFiles) {
			ClassReader classReader = new ClassReader(classFile.getBytes());
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);

			// for Class Interfaces
			{
				for (String value : classNode.interfaces) {
					String className = value.replace("[", "").replace('/', '.');
					if (!className.isEmpty()) {
						classNames.add(className);
					}
				}
			}

			// for Class Super
			{
				classNames.add(classNode.superName.replace('/', '.'));
			}

			Iterator<FieldNode> fields = classNode.fields.iterator();
			while (fields.hasNext()) {
				FieldNode fieldNode = fields.next();

				// for Field Description
				{
					String[] values = fieldNode.desc.split(";");

					for (String value : values) {
						String className = value.replace("[", "").substring(1).replace('/', '.');

						if (!className.isEmpty()) {
							classNames.add(className);
						}
					}
				}
			}

			Iterator<MethodNode> methods = classNode.methods.iterator();
			while (methods.hasNext()) {
				MethodNode methodNode = methods.next();

				// for Method Description
				{
					String methodDescription = methodNode.desc;
					String argumentTypes = methodDescription.substring(1, methodDescription.indexOf(')'));
					String returnTypes = methodDescription.substring(methodDescription.indexOf(')') + 1,
							methodDescription.length());

					String[] values = (argumentTypes + returnTypes).split(";");

					for (String value : values) {
						String className = value.replace("[", "").substring(1).replace('/', '.');

						if (!className.isEmpty()) {
							classNames.add(className);
						}
					}
				}

				// for Method Bytecodes
				{
					AbstractInsnNode[] baseMethodNodes = methodNode.instructions.toArray();
					for (AbstractInsnNode abstractInsnNode : baseMethodNodes) {
						if (abstractInsnNode instanceof MethodInsnNode) {
							MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;

							if (methodInsnNode.owner.contains("/")) {
								classNames.add(methodInsnNode.owner.replace("[", "").replace('/', '.'));
							}
						} else if (abstractInsnNode instanceof FieldInsnNode) {
							FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNode;

							if (fieldInsnNode.owner.contains("/")) {
								classNames.add(fieldInsnNode.owner.replace("[", "").replace('/', '.'));
							}
						} else if (abstractInsnNode instanceof FrameNode) {
							FrameNode frameNode = (FrameNode) abstractInsnNode;

							if (frameNode.local != null) {
								for (int i = 0; i < frameNode.local.size(); i++) {
									if (frameNode.local.get(i) != null) {
										if (frameNode.local.get(i).toString().contains("/")) {
											classNames.add(frameNode.local.get(i).toString().replace("[", "")
													.replace('/', '.'));
										}
									}
								}
							}
							if (frameNode.stack != null) {
								for (int i = 0; i < frameNode.stack.size(); i++) {
									if (frameNode.stack.get(i) != null) {
										if (frameNode.stack.get(i).toString().contains("/")) {
											classNames.add(frameNode.stack.get(i).toString().replace("[", "")
													.replace('/', '.'));
										}
									}
								}
							}
						}

					}
				}
			}
		}

		resultClasses.add(clazz);
		for (String className : classNames) {
			if (!className.isEmpty() && !className.contains("$")) {
				try {
					classDynamicLoader.loadClass(className);
				} catch (ClassNotFoundException e) {
					try {
						Class<?> forName = Class.forName(className);

						if (!resultClasses.contains(forName)) {
							recursiveSearch(forName, classDynamicLoader, resultClasses);
						}
					} catch (ClassNotFoundException e1) {
					}
				}
			}
		}

		resultClasses.remove(clazz);
		resultClasses.add(clazz);
	}

	public static Class<?>[] getAllDependenciesClass(Class<?> clazz) throws IOException {
		ClassDynamicLoader classDynamicLoader = new ClassDynamicLoader();
		List<Class<?>> resultClasses = new LinkedList<Class<?>>();

		recursiveSearch(clazz, classDynamicLoader, resultClasses);

		for (Class<?> dependencyClass : resultClasses) {
			ClassFile[] classFiles = ClassFileReader.getClassBytes(dependencyClass);
			for (ClassFile classFile : classFiles) {
				classDynamicLoader.addClassFile(classFile);
			}
		}

		try {
			Class<?> loadClass = (Class<?>) classDynamicLoader.loadClass(clazz.getName());

			loadClass.getDeclaredClasses();
			loadClass.getDeclaredAnnotations();
			loadClass.getDeclaredConstructors();
			loadClass.getDeclaredFields();
			loadClass.getDeclaredMethods();

		} catch (ClassNotFoundException e1) {
			return null;
		}

		return resultClasses.toArray(new Class<?>[resultClasses.size()]);
	}
}
