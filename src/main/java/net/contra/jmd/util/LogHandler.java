package net.contra.jmd.util;

import net.contra.jmd.Deobfuscator;

/**
 * Created by IntelliJ IDEA.
 * User: Eric
 * Date: Nov 27, 2010
 * Time: 8:51:16 PM
 */
public class LogHandler {
    private String _className = "NoClass";

    public LogHandler(String className) {
        _className = className;
    }

    public void message(String msg) {
        System.out.println(msg);
    }

    public void log(String msg) {
        System.out.println("[" + _className + "]" + msg);
    }

    public void debug(String msg) {
        if (Deobfuscator.debug) {
            System.out.println("[" + _className + "]" + "[DEBUG]" + msg);
        }
    }

    public void error(String msg) {
        System.out.println("[" + _className + "]" + "[ERROR]" + msg);
    }
}
