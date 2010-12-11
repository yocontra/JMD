package net.contra.jmd.generic;

import net.contra.jmd.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * Created by IntelliJ IDEA.
 * User: Eric
 * Date: Nov 30, 2010
 * Time: 4:52:48 AM
 */
public class Renamer {
	private static LogHandler logger = new LogHandler("Renamer");
	private Map<String, ClassGen> cgs = new HashMap<String, ClassGen>();
	//ClassName, Type<Number>
	String JAR_NAME;

	public Renamer(String jarfile) throws Exception {
		File jar = new File(jarfile);
		JAR_NAME = jarfile;
		JarFile jf = new JarFile(jar);
		Enumeration<JarEntry> entries = jf.entries();
		while(entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if(entry == null) {
				break;
			}
			if(entry.isDirectory()) {
			}
			if(entry.getName().endsWith(".class")) {
				ClassGen cg = new ClassGen(new ClassParser(jf.getInputStream(entry), entry.getName()).parse());
				cgs.put(cg.getClassName(), cg);
			} else {
				NonClassEntries.add(entry, jf.getInputStream(entry));
			}
		}
	}

	public void renameFields() {
		for(ClassGen cg : cgs.values()) {
			int fieldCount = 0;
			for(Method m : cg.getMethods()) {

			}
		}
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
			for(ClassGen classIt : cgs.values()) {
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

	public void transform() {
		logger.log("Generic Transformer");

		logger.log("Deobfuscation finished! Dumping jar...");
		dumpJar(JAR_NAME.replace(".jar", "") + "-deob.jar");
		logger.log("Operation Completed.");

	}
}
