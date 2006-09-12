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

package de.ailis.wlandsuite.game;

import java.io.IOException;
import java.io.InputStream;

import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;


/**
 * The RotatingXorInputStream allows reading the xor-rotation compressed data 
 * from an input stream.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class RotatingXorInputStream extends BitInputStream
{
    /** The encryption byte */ 
    private int enc;
    
    /** The end checksum */
    private int endChecksum;
    
    /** The current checksum */
    private int checksum;
    
    /** The bit reader */
    private BitInputStream bitStream;


    /**
     * Constructor
     * 
     * @param stream
     *            The input stream
     * @throws IOException
     */

    public RotatingXorInputStream(InputStream stream) throws IOException
    {
        this.bitStream = new BitInputStreamWrapper(stream);
        init();
    }
    
    
    /**
     * Initializes the encryption of the stream.
     *
     * @throws IOException
     */
    
    private void init() throws IOException
    {
        int e1, e2;
        
        // Get encryption byte and checksum end marker
        e1 = this.bitStream.readByte();
        e2 = this.bitStream.readByte();
        this.enc = e1 ^ e2;
        this.endChecksum = e1 | (e2 << 8);
        
        // Initialize checksum
        this.checksum = 0;
    }


    /**
     * @see java.io.InputStream#read()
     */

    @Override
    public int read() throws IOException
    {
        int crypted, b;

        // Read crypted byte
        crypted = this.bitStream.readByte();

        // Decrypt the byte
        b = crypted ^ this.enc;
        
        // Update checksum
        this.checksum = (this.checksum - b) & 0xffff;

        // Updated encryption byte
        this.enc = (this.enc + 0x1f) % 0x100;
        
        // Return the decrypted byte
        return b;
    }


    /**
     * Returns the current checksum.
     *
     * @return The current checksum
     */
    
    public int getChecksum()
    {
        return this.checksum;
    }


    /**
     * Returns the end checksum.
     *
     * @return The end checksum
     */
    
    public int getEndChecksum()
    {
        return this.endChecksum;
    }
}
