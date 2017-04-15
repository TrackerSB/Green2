/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.preferenceshelper;

import bayern.steinbrecher.green2.data.EnvironmentHandler;

/**
 * This class is only needed for the installer to determine where to put the version of Green2.
 *
 * @author Stefan Huber
 */
public class PreferencesHelper {

    /**
     * The main method. Prints the absolute path of the system node of Green2.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        //This method is implemented according to http://stackoverflow.com/questions/1320709/preference-api-storage
        String path;
        String subkeyPath = EnvironmentHandler.PREFERENCES_SYSTEM_NODE.absolutePath();
        switch (EnvironmentHandler.CURRENT_OS) {
            case WINDOWS:
                if ("32".equals(System.getProperty("sun.arch.data.model"))
                        && "64".equals(System.getProperty("os.arch"))) {
                    path = "HKEY_LOCAL_MACHINE\\Software\\Wow6432Node\\JavaSoft\\Prefs";
                } else {
                    path = "HKEY_LOCAL_MACHINE\\Software\\JavaSoft\\Prefs";
                }
                path += subkeyPath.replaceAll("/", "\\\\");
                break;
            case LINUX:
                path = System.getProperty("java.util.prefs.systemRoot",
                        System.getProperty("java.home") + "/.systemPrefs")
                        + subkeyPath;
                break;
            default:
                throw new IllegalArgumentException("OS not supported by PreferencesHelper.");
        }

        //NOTE Never print a newline character
        System.out.print(path);
    }
}
