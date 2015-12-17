package net.contra.jmd.transformers.dasho;

import net.contra.jmd.util.LogHandler;
import net.contra.jmd.util.NonClassEntries;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Eric
 * Date: Nov 30, 2010
 * Time: 4:52:48 AM
 */
public class DashOTransformer {
    private static LogHandler logger = new LogHandler("DashOTransformer");
    private Map<String, ClassGen> cgs = new HashMap<String, ClassGen>();
    String JAR_NAME;
    String decryptor = "NOTFOUND";
    String decryptorclass = "NOTFOUND";
    private List<Field> flowObstructors = new LinkedList<Field>();
    private Field controlField = null;
    private String controlClass = "";

    public DashOTransformer(String jarfile) throws Exception {
        File jar = new File(jarfile);
        JAR_NAME = jarfile;
        JarFile jf = new JarFile(jar);
        Enumeration<JarEntry> entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry == null) {
                break;
            }
            if (entry.isDirectory()) {
            }
            if (entry.getName().endsWith(".class")) {
                ClassGen cg = new ClassGen(new ClassParser(jf.getInputStream(entry), entry.getName()).parse());
                cgs.put(cg.getClassName(), cg);
            } else {
                NonClassEntries.add(entry, jf.getInputStream(entry));
            }
        }
    }

    public void removeControlFlow() {
        for (ClassGen cg : cgs.values()) {
            int replaced = 0;
            for (Method m : cg.getMethods()) {
                MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
                InstructionList il = mg.getInstructionList();
                InstructionHandle[] handles = il.getInstructionHandles();
                for (int i = 0; i < handles.length; i++) {
                    if (handles[i].getInstruction() instanceof GOTO) {
                        GOTO origGOTO = (GOTO) handles[i].getInstruction();
                        InstructionHandle origTarget = origGOTO.getTarget();
                        if (origTarget.getInstruction() instanceof GOTO) {
                            handles[i].setInstruction(origTarget.getInstruction());
                            replaced++;
                        }
                    }
                }
                mg.setInstructionList(il);
                mg.setMaxLocals();
                mg.setMaxStack();
                cg.replaceMethod(m, mg.getMethod());
            }
            if (replaced > 0) {
                logger.debug("Replaced " + replaced + " GOTO-GOTOs");
            }
        }
    }

    public void unconditionalBranchTransformer() {
        for (ClassGen cg : cgs.values()) {
            for (Method method : cg.getMethods()) {
                final MethodGen mg = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                if (method.isAbstract() || method.isNative()) {
                    return;
                }
                final InstructionList list = mg.getInstructionList();
                InstructionFinder finder = new InstructionFinder(list);
                final ConstantPoolGen cpg = cg.getConstantPool();
                int branchesSimplified = 0;
                Iterator<InstructionHandle[]> matches = finder.search("IfInstruction");
                while (matches.hasNext()) {
                    InstructionHandle ifHandle = matches.next()[0];
                    InstructionHandle target = ((BranchHandle) ifHandle).getTarget();
                    if (target.getInstruction() instanceof GOTO) {
                        branchesSimplified++;
                        ((BranchHandle) ifHandle).setTarget(((BranchHandle) target).getTarget());
                    }
                }
                matches = finder.search("GOTO GOTO");
                while (matches.hasNext()) {
                    InstructionHandle[] match = matches.next();
                    try {
                        list.delete(match[0]);
                    } catch (TargetLostException tlex) {
                        for (InstructionHandle target : tlex.getTargets()) {
                            for (InstructionTargeter targeter : target.getTargeters()) {
                                targeter.updateTarget(target, match[1]);
                            }
                        }
                    }
                }
                mg.setInstructionList(list);
                mg.setMaxLocals();
                mg.setMaxStack();
                if (branchesSimplified > 0) {
                    logger.debug("simplified " + branchesSimplified + " unconditional branches");
                    cg.replaceMethod(method, mg.getMethod());
                }
            }
        }
    }

    public void exitFlowTransformer() {
        for (ClassGen cg : cgs.values()) {
            int correct = 0;
            for (Method method : cg.getMethods()) {
                final MethodGen mgen = new MethodGen(method, cg.getClassName(), cg.getConstantPool());
                if (!method.isAbstract() && !method.isNative()) {
                    InstructionList list = mgen.getInstructionList();
                    InstructionFinder finder = new InstructionFinder(list);
                    CodeExceptionGen[] exceptionGens = mgen.getExceptionHandlers();
                    for (Iterator<InstructionHandle[]> matches = finder.search(
                            "ASTORE ALOAD (NEW DUP (PushInstruction InvokeInstruction)+ (ALOAD IfInstruction LDC GOTO LDC " +
                                    "INVOKEVIRTUAL)? (PushInstruction InvokeInstruction)* InvokeInstruction+)?");
                         matches.hasNext();) {
                        /* thanks to popcorn89 */
                        InstructionHandle[] match = matches.next();
                        if (!(match[match.length - 1].getInstruction() instanceof ATHROW)) {
                            continue;
                        }
                        InstructionHandle astoreInstr = match[0];
                        InstructionHandle athrowInstr = match[match.length - 1];
                        InstructionHandle toRedirect = athrowInstr.getNext();
                        for (CodeExceptionGen exgen : exceptionGens) {
                            if (exgen.getHandlerPC().equals(astoreInstr)) {
                                mgen.removeExceptionHandler(exgen);
                            }
                        }
                        try {
                            list.delete(astoreInstr, athrowInstr);
                        } catch (TargetLostException tlex) {
                            if (athrowInstr == list.getEnd()) {
                                toRedirect = astoreInstr.getPrev();
                            }
                            for (InstructionHandle target : tlex.getTargets()) {
                                for (InstructionTargeter targeter : target.getTargeters()) {
                                    targeter.updateTarget(target, toRedirect);
                                }
                            }
                        }
                    }
                    list.setPositions(true);
                    InstructionHandle lastInstr = list.getEnd();
                    InstructionHandle secondToLastInstr = lastInstr.getPrev();
                    if (secondToLastInstr != null && (lastInstr.getInstruction() instanceof RETURN)
                            && (secondToLastInstr.getInstruction() instanceof RETURN)) {
                        try {
                            list.delete(secondToLastInstr);
                        } catch (TargetLostException tlex) {
                            for (InstructionHandle target : tlex.getTargets()) {
                                for (InstructionTargeter targeter : target.getTargeters()) {
                                    targeter.updateTarget(target, lastInstr);
                                }
                            }
                        }
                    }
                    if (mgen.getMethod() != method) {
                        correct++;
                        //logger.debug("corrected exit flow in " + cg.getClassName() + "." + mgen.getName() + mgen.getSignature());
                        mgen.setInstructionList(list);
                        mgen.setMaxLocals();
                        mgen.setMaxStack();
                        cg.replaceMethod(method, mgen.getMethod());
                    }
                }
            }
            logger.debug("Corrected exit flow " + correct + " times in " + cg.getClassName());
        }
    }

    public static String decrypt(String input) {
        char[] inputChars = input.toCharArray();
        int length = inputChars.length - 1;
        char[] inputCharsCopy = new char[length];
        int lastChar = inputChars[length];
        int j = 0;
        while (j < length) {
            inputCharsCopy[j] = (char) (inputChars[j] ^ lastChar & 127);
            lastChar++;
            j++;
        }
        return new String(inputCharsCopy);
    }

    public void setDecryptor() {
        for (ClassGen cg : cgs.values()) {
            for (Method m : cg.getMethods()) {
                try {
                    if (m.isPublic() && m.isStatic() &&
                            m.getArgumentTypes()[0].toString().equals("java.lang.String")
                            && m.getReturnType().toString().equals("java.lang.String")) {
                        String dc = cg.getClassName() + "." + m.getName();
                        decryptor = m.getName();
                        decryptorclass = cg.getClassName();
                        logger.debug("Found String Decryptor! " + dc);
                        return;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        logger.error("String decrypt not found!");
    }

    public void removeStringEncryption() {
        for (ClassGen cg : cgs.values()) {
            for (Method m : cg.getMethods()) {
                MethodGen mg = new MethodGen(m, cg.getClassName(), cg.getConstantPool());
                InstructionList il = mg.getInstructionList();
                InstructionHandle[] handles = il.getInstructionHandles();
                for (int i = 1; i < handles.length; i++) {
                    if ((handles[i].getInstruction() instanceof LDC)
                            && (handles[i + 1].getInstruction() instanceof INVOKESTATIC)) {
                        INVOKESTATIC invoke = (INVOKESTATIC) handles[i + 1].getInstruction();
                        if (decryptor.equals("NOTFOUND")) {
                            logger.error("String Decryption Method not Set!");
                            return;
                        }
                        String call = invoke.getClassName(cg.getConstantPool());
                        String mcall = invoke.getMethodName(cg.getConstantPool());
                        if (call.equals(decryptorclass) && mcall.equals(decryptor)) {
                            LDC orig = ((LDC) handles[i].getInstruction());
                            String enc = (String) orig.getValue(cg.getConstantPool());
                            int index = cg.getConstantPool().addString(decrypt(enc));
                            LDC lc = new LDC(index);
                            handles[i].setInstruction(lc);
                            handles[i + 1].setInstruction(new NOP());
                            logger.debug(enc + " -> " + decrypt(enc));
                        }
                    }
                }
                mg.setInstructionList(il);
                mg.setMaxLocals();
                mg.setMaxStack();
                cg.replaceMethod(m, mg.getMethod());
            }
        }
    }

    public void dumpJar(String path) {
        FileOutputStream os;
        try {
            os = new FileOutputStream(new File(path));
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException("could not create file \"" + path + "\": " + fnfe);
        }
        JarOutputStream jos;

        try {
            jos = new JarOutputStream(os);
            for (ClassGen classIt : cgs.values()) {
                jos.putNextEntry(new JarEntry(classIt.getClassName().replace('.', File.separatorChar) + ".class"));
                jos.write(classIt.getJavaClass().getBytes());
                jos.closeEntry();
                jos.flush();
            }
            for (JarEntry jbe : NonClassEntries.entries) {
                JarEntry destEntry = new JarEntry(jbe.getName());
                byte[] bite = IOUtils.toByteArray(NonClassEntries.ins.get(jbe));
                jos.putNextEntry(destEntry);
                jos.write(bite);
                jos.closeEntry();
            }
            jos.closeEntry();
            jos.close();
        } catch (IOException ioe) {
        }
    }

    public void transform() {
        logger.log("DashO Deobfuscator");
        logger.log("Finding String Decryption Method...");
        setDecryptor();
        logger.log("Starting String Encryption Removal...");
        removeStringEncryption();
        //logger.log("Starting Unconditional Branch Remover...");
        //removeControlFlow();
        //unconditionalBranchTransformer();
        //logger.log("Starting Exit Flow Corrector...");
        //exitFlowTransformer();
        logger.log("Deobfuscation finished! Dumping jar...");
        dumpJar(JAR_NAME.replace(".jar", "") + "-deob.jar");
        logger.log("Operation Completed.");

    }
}
