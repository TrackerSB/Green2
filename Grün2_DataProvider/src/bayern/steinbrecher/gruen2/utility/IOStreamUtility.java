/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.utility;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements methods for writing data into files.
 *
 * @author Stefan Huber
 */
public final class IOStreamUtility {

    private static final int BYTEBUFFER_SIZE = 1024;

    /**
     * Default constructor.
     */
    private IOStreamUtility() {
        throw new UnsupportedOperationException(
                "Construction of a new object is not allowed.");
    }

    /**
     * Reads the hole content of a given {@code InputStream}.
     *
     * @param inputStream The {@code InputStream} to read.
     * @return The content of the given {@code InputStream}. It never returns
     * {@code null}.
     * @throws IOException Thrown if an {@code IOException} is thrown by
     * {@code InputStream::read}.
     * @see InputStream#read()
     */
    public static String readAll(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();

        try (ReadableByteChannel rbc = Channels.newChannel(inputStream)) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BYTEBUFFER_SIZE);
            CharBuffer charBuffer;
            int bytesRead = rbc.read(byteBuffer);
            while (bytesRead != -1) {
                byteBuffer.flip();
                charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
                output.append(charBuffer);
                byteBuffer.clear();
                bytesRead = rbc.read(byteBuffer);
            }
        }

        return output.toString();
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
            //To make no UTF-8 without BOM but with BOM (Big Endian).
            if (withBom) {
                bw.append('\uFEFF');
            }
            bw.append(content);
        } catch (IOException ex) {
            Logger.getLogger(IOStreamUtility.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
