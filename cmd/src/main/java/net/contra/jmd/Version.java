package net.contra.jmd;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class Version {

    private static final String UNKNOWN = "UNKNOWN";

    private Version() {
    }

    public static String getVersion() {

        URL url = Version.class.getResource(JarFile.MANIFEST_NAME);

        try {
            Manifest manifest = new Manifest(url.openStream());
            return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        } catch (IOException e) {
           // empty
        }

        return UNKNOWN;
    }
}
