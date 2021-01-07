package bayern.steinbrecher.green2.sharedBasis.utility;

import bayern.steinbrecher.green2.sharedBasis.data.SupportedOS;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods for calling programs of Green2 like configuration dialog or the main application.
 *
 * @author Stefan Huber
 */
public enum Programs {
    CONFIGURATION_DIALOG("ConfigurationDialog"),
    LAUNCHER("Launcher"),
    MEMBER_MANAGEMENT("MemberManagement"),
    UNINSTALLER("Uninstaller");

    private static final Logger LOGGER = Logger.getLogger(Programs.class.getName());
    private final String executablePrefix;
    private final String[] options;

    Programs(String executablePrefix, String... options) {
        this.executablePrefix = executablePrefix;
        this.options = Arrays.copyOf(options, options.length);
    }

    /**
     * Calls this program and closes the current program.
     */
    public void call() {
        String[] args = new String[options.length + 1];
        String execFormat = switch (SupportedOS.CURRENT) {
            case LINUX -> "";
            case WINDOWS -> ".bat";
        };
        args[0] = PathUtility.EXECUTABLES_ROOT.resolve(executablePrefix + execFormat)
                .toAbsolutePath()
                .toString();
        System.arraycopy(options, 0, args, 1, options.length);
        Platform.setImplicitExit(false);
        try {
            Process nextProgramProcess = new ProcessBuilder(args)
                    .start();
            // FIXME Piping stdout and stderr has HUGE performance issues
            // nextProgramProcess.waitFor();
            // String errorMessage = IOStreamUtility
            //         .readAll(nextProgramProcess.getErrorStream(), Charset.defaultCharset());
            // if (!errorMessage.isBlank()) {
            //     LOGGER.log(Level.SEVERE, String.format("The started program yielded errors:\n%s", errorMessage));
            // }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Starting the next program failed", ex);
        }
        Platform.exit();
    }
}
