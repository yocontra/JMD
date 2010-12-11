package net.contra.jmd.generic;

import net.contra.jmd.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
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

	public void transform() {
		logger.log("Generic Transformer");
		renameFields();
		logger.log("Deobfuscation finished! Dumping jar...");
		GenericMethods.dumpJar(JAR_NAME, cgs.values());
		logger.log("Operation Completed.");

	}
}
