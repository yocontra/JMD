package net.contra.jmd;

import net.contra.jmd.util.LogHandler;
import org.apache.bcel.generic.ClassGen;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Eric
 * Date: 12/13/10
 * Time: 10:45 PM
 */
public interface Transformer {
	void dump();
	void transform();
}
