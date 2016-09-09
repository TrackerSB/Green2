package bayern.steinbrecher.gruen2.data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements methods for writing data into files.
 *
 * @author Stefan Huber
 */
public final class Output {

    /**
     * Default constructor.
     */
    private Output() {
        throw new UnsupportedOperationException(
                "Construction of a new object is not allowed.");
    }

    /**
     * Overrides the hole content of {@code pathToFile} with {@code content}. If
     * {@code pathToFile} doesn´t exist it creates one.
     *
     * @param content The content to be written into the file.
     * @param pathToFile The file to write in.
     * @param withBom Only if {@code true} it adds '\uFEFF' to the beginning of
     * the file.
     */
    public static void printContent(String content, String pathToFile,
            boolean withBom) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(pathToFile), "UTF-8"))) {
            //To make no UTF-8 without BOM but with BOM.
            if (withBom) {
                bw.append('\uFEFF');
            }
            bw.append(content);
        } catch (IOException ex) {
            Logger.getLogger(Output.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
