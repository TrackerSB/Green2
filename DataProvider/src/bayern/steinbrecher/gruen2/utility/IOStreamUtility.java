/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bayern.steinbrecher.gruen2.utility;

import java.io.BufferedWriter;
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
     * @param charset     The charset to use for decoding the {@code InputStream}.
     * @return The content of the given {@code InputStream}. It never returns
     * {@code null}.
     * @throws IOException Thrown if an {@code IOException} is thrown by
     *                     {@code InputStream::read}.
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
     * Transfers all chars from {@code inputStream} to {@code outputStream} of a
     * file. It loops over {@code inputStream} transfering {@code bytesPerLoop}
     * bytes per loop.
     *
     * @param inputStream  The stream to read from.
     * @param outputStream The stream of the file to write to.
     * @param size         The size of the {@code inputStream}.
     * @param bytesPerLoop The amount of bytes to transfer per loop.
     * @param callback     A method to call on every loop. {@code null} for no
     *                     callback.
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
     * Overrides the hole content of {@code pathToFile} with {@code content}. If
     * {@code pathToFile} doesn´t exist it creates one.
     *
     * @param content    The content to be written into the file.
     * @param pathToFile The file to write in.
     * @param withBom    Only if {@code true} it adds '\uFEFF' to the beginning of
     *                   the file.
     */
    public static void printContent(String content, String pathToFile, boolean withBom) {
        try (BufferedWriter bw
                     = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathToFile), "UTF-8"))) {
            //To make no UTF-8 without BOM but with BOM (Big Endian).
            if (withBom) {
                bw.append('\uFEFF');
            }
            bw.append(content);
        } catch (IOException ex) {
            Logger.getLogger(IOStreamUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
