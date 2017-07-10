package powershellcommandtest;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stefan Huber
 */
public class PowershellCommandTest {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        try {
            //Process powershell = new ProcessBuilder("powershell.exe", "-command \"Start-Process cmd -NoNewWindow -ArgumentList '/c %CD% && java -jar Helper.jar' -Verb runas\"").start();
            ProcessBuilder helperProcessBuilder;
            if (EnvironmentHandler.CURRENT_OS == EnvironmentHandler.OS.WINDOWS) {
                helperProcessBuilder = new ProcessBuilder("powershell", "Start-Process java -ArgumentList '-jar \"G:\\Documents\\NetBeansProjects\\Green2\\Helper\\target\\Helper.jar\"' -Verb runAs");
            } else {
                helperProcessBuilder = new ProcessBuilder("sudo", "java", "-jar", "Helper.jar");
            }
            Process helperProcess = helperProcessBuilder.start();
            helperProcess.waitFor();
            System.out.println(IOStreamUtility.readAll(helperProcess.getErrorStream(), Charset.defaultCharset()));
        } catch (InterruptedException ex) {
            Logger.getLogger(PowershellCommandTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
