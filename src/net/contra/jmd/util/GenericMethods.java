package net.contra.jmd.util;

import org.apache.bcel.generic.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 2, 2010
 * Time: 8:29:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenericMethods {
	public static boolean isInt(Instruction ins) {
		if(((ins instanceof BIPUSH) || (ins instanceof SIPUSH) || (ins instanceof ICONST))) {
			return true;
		} else {
			return false;
		}
	}

	public static int getValueOfInt(Instruction ins, ConstantPoolGen cpg) {
		if(ins instanceof BIPUSH) {
			return ((BIPUSH) ins).getValue().intValue();
		} else if(ins instanceof SIPUSH) {
			return ((SIPUSH) ins).getValue().intValue();
		} else {
			return ((ICONST) ins).getValue().intValue();
		}
	}

    public static String getCallSignature(Instruction ins, ConstantPoolGen cp) {
		if(ins instanceof INVOKESTATIC) {
			INVOKESTATIC invst = (INVOKESTATIC) ins;
			return invst.getSignature(cp);
		} else if(ins instanceof INVOKEVIRTUAL) {
			INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
			return invst.getSignature(cp);
		} else if(ins instanceof INVOKEINTERFACE) {
			INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
			return invst.getSignature(cp);
		} else if(ins instanceof INVOKESPECIAL) {
			INVOKESPECIAL invst = (INVOKESPECIAL) ins;
			return invst.getSignature(cp);
		} else {
			return null;
		}
	}

	public static String getCallClassName(Instruction ins, ConstantPoolGen cp) {
		if(ins instanceof INVOKESTATIC) {
			INVOKESTATIC invst = (INVOKESTATIC) ins;
			return invst.getClassName(cp);
		} else if(ins instanceof INVOKEVIRTUAL) {
			INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
			return invst.getClassName(cp);
		} else if(ins instanceof INVOKEINTERFACE) {
			INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
			return invst.getClassName(cp);
		} else if(ins instanceof INVOKESPECIAL) {
			INVOKESPECIAL invst = (INVOKESPECIAL) ins;
			return invst.getClassName(cp);
		} else {
			return null;
		}
	}

	public static String getCallMethodName(Instruction ins, ConstantPoolGen cp) {
		if(ins instanceof INVOKESTATIC) {
			INVOKESTATIC invst = (INVOKESTATIC) ins;
			return invst.getMethodName(cp);
		} else if(ins instanceof INVOKEVIRTUAL) {
			INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
			return invst.getMethodName(cp);
		} else if(ins instanceof INVOKEINTERFACE) {
			INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
			return invst.getMethodName(cp);
		} else if(ins instanceof INVOKESPECIAL) {
			INVOKESPECIAL invst = (INVOKESPECIAL) ins;
			return invst.getMethodName(cp);
		} else {
			return null;
		}
	}

	public static Type[] getCallArgTypes(Instruction ins, ConstantPoolGen cp) {
		if(ins instanceof INVOKESTATIC) {
			INVOKESTATIC invst = (INVOKESTATIC) ins;
			return invst.getArgumentTypes(cp);
		} else if(ins instanceof INVOKEVIRTUAL) {
			INVOKEVIRTUAL invst = (INVOKEVIRTUAL) ins;
			return invst.getArgumentTypes(cp);
		} else if(ins instanceof INVOKEINTERFACE) {
			INVOKEINTERFACE invst = (INVOKEINTERFACE) ins;
			return invst.getArgumentTypes(cp);
		} else if(ins instanceof INVOKESPECIAL) {
			INVOKESPECIAL invst = (INVOKESPECIAL) ins;
			return invst.getArgumentTypes(cp);
		} else {
			return null;
		}
	}

	public static boolean isCall(Instruction ins) {
		if(ins instanceof INVOKESTATIC) {
			return true;
		} else if(ins instanceof INVOKEVIRTUAL) {
			return true;
		} else if(ins instanceof INVOKEINTERFACE) {
			return true;
		} else if(ins instanceof INVOKESPECIAL) {
			return true;
		} else {
			return false;
		}
	}
}
