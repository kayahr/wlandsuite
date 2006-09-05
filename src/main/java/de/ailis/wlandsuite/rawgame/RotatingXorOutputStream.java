/*
 * $Id$
 * Copyright (C) 2006 Klaus Reimer <k@ailis.de>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.ailis.wlandsuite.rawgame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * RotatingXorOutputStream
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class RotatingXorOutputStream extends OutputStream
{
    /** The temporary output stream */
    private ByteArrayOutputStream stream;

    /** The original output stream */
    private OutputStream origStream;
    

    /**
     * Constructor
     * 
     * @param stream
     *            The output stream
     */

    public RotatingXorOutputStream(OutputStream stream)
    {
        this.stream = new ByteArrayOutputStream();
        this.origStream = stream;
    }


    /**
     * @see java.io.OutputStream#write(int)
     */

    @Override
    public void write(int b)
    {
        this.stream.write(b);
    }


    /**
     * @see java.io.OutputStream#flush()
     */

    @Override
    public void flush() throws IOException
    {
        byte[] bytes;
        int enc;
        int checksum;
        int endChecksum = 0;

        // Get the written bytes
        bytes = this.stream.toByteArray();

        // Calculate the end checksum
        checksum = 0;
        for (byte b: bytes)
        {
            checksum = (checksum - (b & 0xff)) & 0xffff;
            endChecksum = checksum;
        }

        // Write the end checksum
        this.origStream.write(endChecksum & 0xff);
        this.origStream.write(endChecksum >> 8);
        
        // Calculate initial encryptor
        enc = (endChecksum & 0xff) ^ (endChecksum >> 8);

        // Write the encrypted bytes
        for (byte b: bytes)
        {
            // Write encrypted byte
            this.origStream.write(b ^ enc);

            // Update encryptor
            enc = (enc + 0x1f) % 0x100;
        }
    }


    /**
     * @see java.io.OutputStream#close()
     */

    @Override
    public void close() throws IOException
    {
        flush();
    }
}
