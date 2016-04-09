package org.gdg.frisbee.android.utils;

import java.io.File;

public class FileUtils {

    private FileUtils() {
        //No instance
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
        }

        return dir.delete();
    }
}
