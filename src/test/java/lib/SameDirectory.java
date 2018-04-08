package lib;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.CodeSource;

public final class SameDirectory {

    public static String getPath(Class<?> clazz) {
        CodeSource source = clazz.getProtectionDomain().getCodeSource();
        if (source != null) {
            String path = source.getLocation().getPath();
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException impossible) {
            }
            if (System.getProperty("file.separator").equals("\\") && (path.startsWith("/") || path.startsWith("\\"))) {
                path = path.substring(1);
            }
            return path;
        }
        return null;
    }

    public static String getPackagePath(Class<?> clazz) {
        String path = getPath(clazz);
        if (path != null) {
            path += clazz.getPackage().getName().replace(".", "/");
        }
        return path;
    }

    private SameDirectory() {
        throw new AssertionError();
    }
}
