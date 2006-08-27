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

package de.ailis.wlandsuite.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * The bit output stream can be used to write a stream bit by bit. But it also
 * provides other useful methods like writing 16 or 32 bit values which also
 * works in a not-byte aligned stream. So if you write 4 bits then you are still
 * able to write the next 16 bits as a word.
 * 
 * If you have written not-byte aligned data (for example just 7 bits instead of
 * 8) then you MUST flush() the stream so these 8 bits are written (With an
 * appended zero bit). If you close() the stream then flush() is called
 * automatically.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class BitOutputStream extends OutputStream
{
    /** The current byte */
    private int currentByte;

    /** The current bit */
    private byte currentBit = 0;


    /**
     * Writes a bit to the output stream.
     * 
     * @param bit
     *            The bit to write
     * @throws IOException
     */

    public void writeBit(byte bit) throws IOException
    {
        this.currentByte = (this.currentByte << 1) | (bit & 1);
        this.currentBit++;
        if (this.currentBit > 7)
        {
            write(this.currentByte);
            this.currentByte = 0;
            this.currentBit = 0;
        }
    }


    /**
     * Writes a bit to the output stream.
     * 
     * @param bit
     *            The bit to write
     * @throws IOException
     */

    public void writeBit(boolean bit) throws IOException
    {
        writeBit((byte) (bit ? 1 : 0));
    }


    /**
     * Writes a byte to the stream.
     * 
     * @param b
     *            The byte to write
     * @throws IOException
     */

    public void writeByte(int b) throws IOException
    {
        if (this.currentBit == 0)
        {
            write(b);
        }
        else
        {
            for (int i = 7; i >= 0; i--)
            {
                writeBit((byte) ((b >> i) & 1));
            }
        }
    }


    /**
     * Writes a signed byte.
     * 
     * @param b
     *            The byte to write
     * @throws IOException
     */
    
    public void writeSignedByte(int b) throws IOException
    {
        if (b < 0)
        {
            writeByte(b + 256);
        }
        else
        {
            writeByte(b);
        }
    }


    /**
     * Writes a 2-byte word to the stream.
     * 
     * @param word
     *            The word to write
     * @throws IOException
     */

    public void writeWord(int word) throws IOException
    {
        writeByte(word & 0xff);
        writeByte((word >> 8) & 0xff);
    }


    /**
     * Writes a 4-byte integer to the stream.
     * 
     * @param integer
     *            The integer to write
     * @throws IOException
     */

    public void writeInt(long integer) throws IOException
    {
        writeByte((int) (integer & 0xff));
        writeByte((int) ((integer >> 8) & 0xff));
        writeByte((int) ((integer >> 16) & 0xff));
        writeByte((int) ((integer >> 24) & 0xff));
    }


    /**
     * Flush the output to make sure all bits are written even if they don't
     * fill a whole byte.
     * 
     * @throws IOException
     */

    @Override
    public void flush() throws IOException
    {
        if (this.currentBit != 0)
        {
            write(this.currentByte << (8 - this.currentBit));
        }
    }


    /**
     * Closes the connected output stream and makes sure the last byte is
     * written.
     * 
     * @throws IOException
     */

    @Override
    public void close() throws IOException
    {
        flush();
        super.close();
    }
}
