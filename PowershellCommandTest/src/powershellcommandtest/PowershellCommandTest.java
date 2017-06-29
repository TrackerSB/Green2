/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package powershellcommandtest;

import java.io.IOException;

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
        Process powershell = new ProcessBuilder("powershell.exe", "-command \"Start-Process cmd -NoNewWindow -ArgumentList '/c %CD% && java -jar Helper.jar' -Verb runas\"").start();
        System.out.println(powershell.getErrorStream().available());
    }
}
