package net.contra.jmd;

import net.contra.jmd.transformers.allatori.AllatoriTransformer;
import net.contra.jmd.transformers.dasho.DashOTransformer;
import net.contra.jmd.transformers.generic.*;
import net.contra.jmd.transformers.jshrink.JShrinkTransformer;
import net.contra.jmd.transformers.smokescreen.SmokeScreenTransformer;
import net.contra.jmd.transformers.zkm.ZKMTransformer;
import net.contra.jmd.util.LogHandler;

import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * User: Eric
 * Date: Nov 24, 2010
 * Time: 9:21:10 PM
 */
public class Deobfuscator {
    public static boolean debug = false;
    private static double version = 1.6;
    private static String credits = "skoalman, super_, ollie, popcorn89, the prophecy, and saevion";
    private static LogHandler logger = new LogHandler("Deobfuscator");
    //TODO: Load single class files... herpa derp

    public static void main(String[] argv) throws Exception {
        logger.message("Java Multi-Purpose Deobfuscator");
        logger.message("Please Visit RECoders.org for updates and info");
        logger.message("Version " + version);
        logger.message("Created by Contra. Please read LICENSE.txt");
        logger.message("Tons of code from " + credits);
        if (!(argv.length >= 3)) {
            logger.error("java -jar JMD.jar <file location> <type> <debug true/false> <optional args>");
            logger.error("Example ZKM: java -jar JMD.jar \"C:/Files/Magic.jar\" zkm true");
            logger.error("Example StringScan: java -jar JMD.jar \"C:/Files/Magic.jar\" stringscanner \"rscheata.net\" false");
            logger.error("Example StringReplace: java -jar JMD.jar \"C:/Files/Magic.jar\" stringreplacer \"rscheata.net\" true \"rscbunlocked.net\"");
            return;
        }
        if (argv[2].equals("true")) {
            debug = true;
        }
        if (argv[1].toLowerCase().equals("zkm")) {
            ZKMTransformer zt = new ZKMTransformer(argv[0]);
            zt.transform();
        } else if (argv[1].toLowerCase().equals("allatori")) {
            AllatoriTransformer at = new AllatoriTransformer(argv[0], false);
            at.transform();
        } else if (argv[1].toLowerCase().equals("allatori-strong")) {
            AllatoriTransformer at = new AllatoriTransformer(argv[0], true);
            at.transform();
        } else if (argv[1].toLowerCase().equals("jshrink")) {
            JShrinkTransformer jt = new JShrinkTransformer(argv[0]);
            jt.transform();
        } else if (argv[1].toLowerCase().equals("dasho")) {
            DashOTransformer dt = new DashOTransformer(argv[0]);
            dt.transform();
        } else if (argv[1].toLowerCase().equals("smokescreen")) {
            SmokeScreenTransformer st = new SmokeScreenTransformer(argv[0]);
            st.transform();
        } else if (argv[1].toLowerCase().equals("renamer")) {
            Renamer re = new Renamer(argv[0]);
            re.transform();
        } else if (argv[1].toLowerCase().equals("genericstringdeobfuscator")) {
            GenericStringDeobfuscator gsd = new GenericStringDeobfuscator(argv[0]);
            gsd.transform();
        } else if (argv[1].toLowerCase().equals("stringfixer")) {
            StringFixer sf = new StringFixer(argv[0]);
            sf.transform();
        } else if (argv[1].toLowerCase().equals("stackfixer")) {
            StackFixer sf = new StackFixer(argv[0]);
            sf.transform();
        } else if (argv[1].toLowerCase().equals("foreigncallremover")) {
            ForeignCallRemover fc = new ForeignCallRemover(argv[0]);
            fc.transform();
        } else if (argv[1].toLowerCase().equals("stringscanner")) {
            StringScanner us = new StringScanner(argv[0], argv[3], false, "");
            us.scan();
        } else if (argv[1].toLowerCase().equals("stringreplacer")) {
            StringScanner us = new StringScanner(argv[0], argv[3], true, argv[4]);
            us.scan();
        } else {
            logger.error("Types are: ZKM, Allatori, JShrink, DashO, SmokeScreen, StringFixer, StringScanner, " +
                    "\n ForeignCallRemover, GenericStringDeobfuscator, StackFixer, StringFixer, Renamer, and StringReplacer (not case sensitive)");
        }
        Scanner in = new Scanner(System.in);
        logger.message("Press any key to exit...");
        in.nextLine();
    }
}
