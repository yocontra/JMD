package net.contra.jmd.allatori;

import net.contra.jmd.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * Created by IntelliJ IDEA.
 * User: Eric
 * Date: Nov 24, 2010
 * Time: 9:48:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class AllatoriTransformer {
	//Illusion made this
	LogHandler logger = new LogHandler("AllatoriTransformer");
	ArrayList<ClassGen> cgs = new ArrayList<ClassGen>();
	ClassGen ALLATORI_CLASS;
	String JAR_NAME;

	public AllatoriTransformer(String jarfile) throws Exception {
		logger.log("Allatori Deobfuscator");
		File jar = new File(jarfile);
		JAR_NAME = jarfile;
		JarFile jf = new JarFile(jar);
		Enumeration<JarEntry> entries = jf.entries();
		while(entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if(entry == null) {
				break;
			}
			if(entry.getName().endsWith(".class")) {
				ClassGen cg = new ClassGen(new ClassParser(jf
						.getInputStream(entry), entry.getName()).parse());
				if(isStringClass(cg) || isStringClassB(cg)) {
					ALLATORI_CLASS = cg;
					logger.debug("Allatori Class: " + ALLATORI_CLASS.getClassName());
				} else {
					cgs.add(cg);
				}
			} else {
				NonClassEntries.add(entry, jf.getInputStream(entry));
			}
		}
	}

	private boolean isStringClass(ClassGen cg) {
		if(cg.getMethods().length == 2 && cg.getMethods()[0].isStatic()
				&& cg.getMethods()[1].isStatic()) {
			if(cg.getMethods()[0].getReturnType().toString().equals(
					"java.lang.String")
					&& cg.getMethods()[1].getReturnType().toString().equals(
					"java.lang.String")) {
				return true;
			}
		}
		return false;
	}

	private boolean isStringClassB(ClassGen cg) {
		if(cg.getMethods().length == 1 && cg.getMethods()[0].isStatic()) {
			if(cg.getMethods()[0].getReturnType().toString().equals("java.lang.String")) {
				return true;
			}
		}
		return false;
	}

	public static String decrypt(String string) {
		int i = 85;
		char[] cs = new char[string.length()];
		int pos = cs.length - 1;
		int index = pos;
		int xor = i;
		while(pos >= 0) {
			char c = (char) (string.charAt(index) ^ xor);
			int c1_index = index;
			xor = (char) ((char) (c1_index ^ xor) & '?');
			cs[c1_index] = c;
			if(--index < 0) {
				break;
			}
			char c2 = (char) (string.charAt(index) ^ xor);
			int c2_index = index;
			xor = (char) ((char) (c2_index ^ xor) & '?');
			cs[c2_index] = c2;
			pos = --index;
		}

		return new String(cs);
	}

	public void dumpJar(String path) {
		FileOutputStream os;
		try {
			os = new FileOutputStream(new File(path));
		} catch(FileNotFoundException fnfe) {
			throw new RuntimeException("could not create file \"" + path + "\": " + fnfe);
		}
		JarOutputStream jos;

		try {
			jos = new JarOutputStream(os);
			for(ClassGen classIt : cgs) {
				jos.putNextEntry(new JarEntry(classIt.getClassName().replace('.', File.separatorChar) + ".class"));
				jos.write(classIt.getJavaClass().getBytes());
				jos.closeEntry();
				jos.flush();
			}
			for(JarEntry jbe : NonClassEntries.entries) {
				JarEntry destEntry = new JarEntry(jbe.getName());
				byte[] bite = IOUtils.toByteArray(NonClassEntries.ins.get(jbe));
				jos.putNextEntry(destEntry);
				jos.write(bite);
				jos.closeEntry();
			}
			jos.closeEntry();
			jos.close();
		} catch(IOException ioe) {
		}
	}

	public void transform() throws TargetLostException {
		logger.log("Starting Encrypted String Removal...");
		replaceStrings();
		logger.log("Deobfuscation Finished! Dumping jar...");
		dumpJar(JAR_NAME.replace(".jar", "") + "-deob.jar");
		logger.log("Operation Completed.");
	}

	public void ObfuscateStrings() {
		for(ClassGen cg : cgs) {
			int replaced = 0;
			for(Method method : cg.getMethods()) {
				MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
				InstructionList list = mg.getInstructionList();
				if(list == null) {
					continue;
				}
				InstructionFinder finder = new InstructionFinder(list);
				Iterator<InstructionHandle[]> matches = finder.search("LDC");
				while(matches.hasNext()) {
					InstructionHandle[] match = matches.next();
					/*
										INVOKESTATIC inv = new INVOKESTATIC(22);
										list.insert(match, (INSTRUCTION)inv);  */
				}
			}
		}
	}

	public void replaceStrings() throws TargetLostException {
		for(ClassGen cg : cgs) {
			int replaced = 0;
			for(Method method : cg.getMethods()) {
				MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
				InstructionList list = mg.getInstructionList();
				if(list == null) {
					continue;
				}
				InstructionHandle[] handles = list.getInstructionHandles();
				for(int i = 1; i < handles.length; i++) {
					if((handles[i].getInstruction() instanceof INVOKESTATIC) && (handles[i - 1].getInstruction() instanceof LDC)) {
						INVOKESTATIC methodCall = (INVOKESTATIC) handles[i].getInstruction();
						if(methodCall.getClassName(cg.getConstantPool()).contains(ALLATORI_CLASS.getClassName())) {
							LDC encryptedLDC = (LDC) handles[i - 1].getInstruction();
							String encryptedString = encryptedLDC.getValue(cg.getConstantPool()).toString();
							String decryptedString = decrypt(encryptedString);
							logger.debug(encryptedString + " -> " + decryptedString + " in " + cg.getClassName() + "." + method.getName());
							int stringRef = cg.getConstantPool().addString(decryptedString);
							LDC lc = new LDC(stringRef);
							NOP nop = new NOP();
							handles[i].setInstruction(lc);
							handles[i - 1].setInstruction(nop);
							replaced++;
						}
					}
				}
				mg.setInstructionList(list);
				mg.setMaxLocals();
				mg.setMaxStack();
				cg.replaceMethod(method, mg.getMethod());
			}
			if(replaced > 0) {
				logger.debug("decrypted " + replaced + " strings in class " + cg.getClassName());
			}
		}
	}
}
