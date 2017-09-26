package jnitest;

import cz.adamh.utils.NativeUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 */
public class JNITest {

    static {
        try {
            String osname = "Linux"; //Note Just for testing purpose.
            String arch = System.getProperty("os.arch").endsWith("64") ? "64" : "32";
            String fileformat = "so";
            NativeUtils.loadLibraryFromJar("/jnitest/externalLibs/JNITestCpp" + osname + arch + "." + fileformat);
        } catch (IOException ex) {
            Logger.getLogger(JNITest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        printHelloWorld();
    }

    private static native void printHelloWorld();
}