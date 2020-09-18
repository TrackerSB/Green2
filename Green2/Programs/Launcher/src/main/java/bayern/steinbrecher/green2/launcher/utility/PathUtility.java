package bayern.steinbrecher.green2.launcher.utility;

import bayern.steinbrecher.green2.sharedBasis.utility.URLUtility;

/**
 * Contains paths which are needed by multiple other classes.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public final class PathUtility {

    /**
     * The URL of the online repository containing the installation files.
     */
    public static final String PROGRAMFOLDER_PATH_ONLINE
            = URLUtility.resolveURL("https://traunviertler-traunwalchen.de/programme")
                    .orElse("");

    private PathUtility() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }
}
