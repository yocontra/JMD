package net.contra.jmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class Version {

    private static final String UNKNOWN = "UNKNOWN";

    private Version() {
    }

    public static String getVersion() {
        final URL url = Version.class.getResource(JarFile.MANIFEST_NAME);
        if (url == null) {
            return UNKNOWN;
        }

        try {
            final InputStream inputStream = url.openStream();
            final Manifest manifest = new Manifest(inputStream);

            return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        } catch (IOException e) {
            // empty
        }

        return UNKNOWN;
    }
}
