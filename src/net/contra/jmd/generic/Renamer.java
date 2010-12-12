package net.contra.jmd.generic;

import net.contra.jmd.util.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import java.io.File;
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

	public void renameMethods() {
		for(ClassGen cg : cgs.values()) {
			if(cg.isAbstract() || cg.isInterface()) {
				continue;
			}
			int count = 0;
			for(Method m : cg.getMethods()) {
				if(m.getName().equalsIgnoreCase("<clinit>")
						|| m.getName().equalsIgnoreCase("<init>")
						|| m.getName().equalsIgnoreCase("main")
						|| m.isAbstract()
						|| m.isInterface()) {
					continue;
				}
				ConstantPoolGen cpg = cg.getConstantPool();
				String name = "";
				if(m.isStatic()) {
					name += "static";
				}
				String type = m.getReturnType().toString();

				name += type.substring(type.lastIndexOf(".") + 1, type.length());
				name = name.replace("void", "");
				if(name.contains("[]")) {
					name = name.replace("[]", "Array");
					//name += "[]";
				}
				name += "Method" + count;
				//int nameRef = cpg.addNameAndType(name, m.getSignature());
				//cpg.setConstant(m.getNameIndex(), cpg.getConstant(nameRef));
				//TODO: Get it to fully change the name (updated methodref name index) and not corrupt the constant pool lol
				//TODO: Rename classes first, then methods, then fields.
				MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
				mg.setName(name);

				cg.replaceMethod(m, mg.getMethod());
				cg.setConstantPool(cpg);
				count++;
				logger.debug(cg.getClassName() + "." + m.getName() + " -> " + cg.getClassName() + "." + name);
			}
		}
	}

	public void transform() {
		logger.log("Generic Renamer");
		renameMethods();
		logger.log("Deobfuscation finished! Dumping jar...");
		GenericMethods.dumpJar(JAR_NAME, cgs.values());
		logger.log("Operation Completed.");

	}
}
