/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.utility;

import bayern.steinbrecher.green2.data.EnvironmentHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Implements methods for writing data into files.
 *
 * @author Stefan Huber
 */
public final class IOStreamUtility {

    private static final int BUFFER_SIZE = 1024;

    /**
     * Default constructor.
     */
    private IOStreamUtility() {
        throw new UnsupportedOperationException("Construction of a new object is not allowed.");
    }

    /**
     * Opens a dialog asking the user to choose a directory. It does not change any user specific registry keys relating
     * to any profile. If the profile settings should be updated
     * {@link EnvironmentHandler#askForSavePath(javafx.stage.Stage, java.lang.String, java.lang.String, java.lang.Object...)}
     * should be used.
     *
     * @param owner The owner of the dialog.
     * @param filePrefix The name of the file which is prefixed with the current date and may be extended by a number if
     * it already exists.
     * @param fileEnding The format of the file. NOTE: Without leading point.
     * @param initialDirectoryPath The path of the directory to show initially.
     * @return The chosen directory or {@link Optional#empty()} if no directory was chosen.
     * @see EnvironmentHandler#askForSavePath(javafx.stage.Stage, java.lang.String, java.lang.String,
     * java.lang.Object...)
     */
    public static Optional<File> askForSavePath(Stage owner, String filePrefix, String fileEnding,
            String initialDirectoryPath) {
        String today = LocalDate.now().toString();
        String dateFilePrefix = today + "_" + filePrefix;
        File initialDirectory = new File(initialDirectoryPath);
        File initialFile = new File(initialDirectory, dateFilePrefix + "." + fileEnding);
        Random random = new Random();
        while (initialFile.exists()) {
            //CHECKSTYLE.OFF: MagicNumber - Choosing 1000 is quite random and has no special matter.
            initialFile = new File(initialDirectory, dateFilePrefix + "_" + random.nextInt(1000) + "." + fileEnding);
            //CHECKSTYLE.ON: MagicNumber
        }

        FileChooser saveDialog = new FileChooser();
        saveDialog.setTitle(EnvironmentHandler.getResourceValue("save"));
        saveDialog.setInitialDirectory(initialDirectory);
        saveDialog.setInitialFileName(initialFile.getName());
        FileChooser.ExtensionFilter givenExtensionFilter
                = new FileChooser.ExtensionFilter(fileEnding.toUpperCase(Locale.ROOT), "*." + fileEnding);
        saveDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(EnvironmentHandler.getResourceValue("allFiles"), "*.*"),
                givenExtensionFilter);
        saveDialog.setSelectedExtensionFilter(givenExtensionFilter);
        return Optional.ofNullable(saveDialog.showSaveDialog(owner));
    }

    /**
     * Reads the hole content of a given {@link InputStream} decoding with {@link Charset}.
     *
     * @param inputStream The {@link InputStream} to read.
     * @param charset The charset to use for decoding the {@link InputStream}.
     * @return The content of the given {@link InputStream}. It never returns {@code null}.
     * @throws IOException Thrown if an {@link IOException} is thrown by {@link InputStream#read()}.
     * @see InputStream#read()
     */
    public static String readAll(InputStream inputStream, Charset charset) throws IOException {
        StringBuilder output = new StringBuilder();

        try (ReadableByteChannel rbc = Channels.newChannel(inputStream)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            CharBuffer charBuffer;
            while (rbc.read(byteBuffer) != -1) {
                byteBuffer.flip();
                charBuffer = charset.decode(byteBuffer);
                output.append(charBuffer);
                byteBuffer.clear();
            }
        }

        return output.toString();
    }

    /**
     * Transfers all chars from {@code isr} to {@code osw}.
     *
     * @param isr The stream to read from.
     * @param osw The stream to write to.
     */
    public static void transfer(InputStreamReader isr, OutputStreamWriter osw) {
        char[] buffer = new char[BUFFER_SIZE];
        try {
            int bytesRead;
            while ((bytesRead = isr.read(buffer)) > 0) {
                osw.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(IOStreamUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Transfers all chars from {@link InputStream} to {@code outputStream} of a file. It loops over {@link InputStream}
     * transferring {@code bytesPerLoop} bytes per loop.
     *
     * @param inputStream The stream to read from.
     * @param outputStream The stream of the file to write to.
     * @param size The size of the {@link InputStream}.
     * @param bytesPerLoop The amount of bytes to transfer per loop.
     * @param callback A method to call on every loop. {@code null} for no callback.
     */
    public static void transfer(InputStream inputStream, FileOutputStream outputStream, long size, long bytesPerLoop,
            Runnable callback) {
        try (ReadableByteChannel inChannel = Channels.newChannel(inputStream);
                FileChannel outChannel = outputStream.getChannel()) {
            for (long offset = 0; offset < size; offset += bytesPerLoop) {
                outChannel.transferFrom(inChannel, offset, bytesPerLoop);
                if (callback != null) {
                    callback.run();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IOStreamUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Overrides the hole content of {@code outputFile} with {@code content}.If {@code pathToFile} doesn't exist it
     * creates one.
     *
     * @param content The content to be written into the file.
     * @param outputFile The file to write in.
     * @param withBom Only if {@code true} it adds '\uFEFF' to the beginning of the file.
     * @throws java.io.IOException Thrown if any I/O error occurs.
     */
    public static void printContent(String content, File outputFile, boolean withBom) throws IOException {
        try (BufferedWriter bw
                = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            //To make no UTF-8 without BOM but with BOM (Big Endian).
            if (withBom) {
                bw.append('\uFEFF');
            }
            bw.append(content);
        }
    }
}