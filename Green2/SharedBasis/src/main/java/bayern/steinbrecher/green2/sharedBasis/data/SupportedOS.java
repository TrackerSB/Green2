package bayern.steinbrecher.green2.sharedBasis.data;

import java.util.Locale;

public enum SupportedOS {
    LINUX,
    WINDOWS;

    public static final SupportedOS CURRENT
            = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")
            ? SupportedOS.WINDOWS
            : SupportedOS.LINUX;
}
