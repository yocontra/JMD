package net.contra.jmd.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;

public final class NonClassEntries {
    public static ArrayList<JarEntry> entries = new ArrayList<JarEntry>();
    public static Map<JarEntry, InputStream> ins = new HashMap<JarEntry, InputStream>();

    private NonClassEntries() {
    }

    public static JarEntry getByName(String name) {
        for (JarEntry e : entries) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static byte[] readAll(InputStream inputStream) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = 0;
            if (len < 0) {
                break;
            }
            try {
                len = inputStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        byte[] data = bout.toByteArray();
        return data;
    }

    public static void add(JarEntry entry, InputStream inputStream) {
        entries.add(entry);
        ins.put(entry, inputStream);
    }

}
