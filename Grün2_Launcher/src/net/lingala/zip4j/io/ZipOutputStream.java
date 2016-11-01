/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.lingala.zip4j.io;

import java.io.IOException;
import java.io.OutputStream;

import net.lingala.zip4j.model.ZipModel;

public class ZipOutputStream extends DeflaterOutputStream {

    public ZipOutputStream(OutputStream outputStream) {
        this(outputStream, null);
    }

    public ZipOutputStream(OutputStream outputStream, ZipModel zipModel) {
        super(outputStream, zipModel);
    }

    public void write(int bval) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte) bval;
        write(b, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        crc.update(b, off, len);
        updateTotalBytesRead(len);
        super.write(b, off, len);
    }
}
