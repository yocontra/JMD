package net.contra.jmd.transformers.generic;

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
public class ForeignCallRemover {
	private static LogHandler logger = new LogHandler("ForeignCallRemover");
	private Map<String, ClassGen> cgs = new HashMap<String, ClassGen>();
	String JAR_NAME;

	public ForeignCallRemover(String jarfile) throws Exception {
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
				if(!jbe.isDirectory()) {
					JarEntry destEntry = new JarEntry(jbe.getName());
					byte[] bite = IOUtils.toByteArray(NonClassEntries.ins.get(jbe));
					jos.putNextEntry(destEntry);
					jos.write(bite);
					jos.closeEntry();
				}
			}
			jos.closeEntry();
			jos.close();
		} catch(IOException ioe) {
		}
	}

	public void RemoveCalls() {
		for(ClassGen cg : cgs.values()) {
			int replaced = 0;
			for(Method method : cg.getMethods()) {
				MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
				InstructionList list = mg.getInstructionList();
				if(list == null) {
					continue;
				}
				InstructionHandle[] handles = list.getInstructionHandles();
				for(int i = 0; i < handles.length; i++) {
					if(GenericMethods.isCall(handles[i].getInstruction())) {
						String callClass = GenericMethods.getCallClassName(handles[i].getInstruction(), cg.getConstantPool());
						String callMethod = GenericMethods.getCallMethodName(handles[i].getInstruction(), cg.getConstantPool());
						if(!callClass.startsWith("java") &&
								(!callClass.startsWith("org") || callClass.contains("PingBack"))
								&& !cgs.containsKey(callClass)) {
							if(GenericMethods.getCallArgTypes(handles[i].getInstruction(), cg.getConstantPool()).length == 0) {
								handles[i].setInstruction(new NOP());
								logger.debug(callClass + "." + callMethod + " invoke had no arguments, so we NOP");
							} else {
								//TODO: WRITE SOMETHING TO DETECT THE ARGUMENTS AND DELETE THE CODE FOR THEM
								handles[i].setInstruction(new POP());
								logger.debug(callClass + "." + callMethod + " invoke had arguments, so we POP");
							}
							mg.setInstructionList(list);
							mg.removeNOPs();
							mg.setMaxLocals();
							mg.setMaxStack();
							cg.replaceMethod(method, mg.getMethod());
							replaced++;
						}
					}
					if(handles[i].getInstruction() instanceof NEW) {
						String callClass = ((NEW) handles[i].getInstruction()).getLoadClassType(cg.getConstantPool()).getClassName();
						//logger.debug(callClass);
						if(!callClass.startsWith("java") &&
								(!callClass.startsWith("org") || callClass.contains("PingBack"))
								&& !cgs.containsKey(callClass)) {
							handles[i].setInstruction(new NOP());
							logger.debug("NOPed out NEW " + callClass);
						}
						mg.setInstructionList(list);
						mg.removeNOPs();
						mg.setMaxLocals();
						mg.setMaxStack();
						cg.replaceMethod(method, mg.getMethod());
						replaced++;
					}
				}
			}
			if(replaced > 0) {
				logger.debug("Removed " + replaced + " foreign calls in " + cg.getClassName());
			}
		}
	}

	public void transform() {
		logger.log("Foreign Call Remover");
		RemoveCalls();
		logger.log("Deobfuscation finished! Dumping jar...");
		dumpJar(JAR_NAME.replace(".jar", "") + "-deob.jar");
		logger.log("Operation Completed.");

	}
}
