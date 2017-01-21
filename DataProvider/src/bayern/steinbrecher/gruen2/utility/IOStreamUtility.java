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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        throw new UnsupportedOperationException(
                "Construction of a new object is not allowed.");
    }

    /**
     * Reads the hole content of a given {@code InputStream} decoding with
     * {@code charset}.
     *
     * @param inputStream The {@code InputStream} to read.
     * @param charset The charset to use for decoding the {@code InputStream}.
     * @return The content of the given {@code InputStream}. It never returns
     * {@code null}.
     * @throws IOException Thrown if an {@code IOException} is thrown by
     * {@code InputStream::read}.
     * @see InputStream#read()
     */
    public static String readAll(InputStream inputStream, Charset charset)
            throws IOException {
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
            Logger.getLogger(IOStreamUtility.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Transfers all chars from {@code inputStream} to {@code outputStream} of a
     * file. It loops over {@code inputStream} transfering {@code bytesPerLoop}
     * bytes per loop.
     *
     * @param inputStream The stream to read from.
     * @param outputStream The stream of the file to write to.
     * @param size The size of the {@code inputStream}.
     * @param bytesPerLoop The amount of bytes to transfer per loop.
     * @param callback A method to call on every loop. {@code null} for no
     * callback.
     */
    public static void transfer(InputStream inputStream,
            FileOutputStream outputStream, long size, long bytesPerLoop,
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
            Logger.getLogger(IOStreamUtility.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
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
