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
 * A wrapper class to wrap any output stream into a BitOutputStream.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class SeekableOutputStream extends OutputStream
{
    /** The internal byte cache */
    protected byte cache[];

    /** The logical buffer size (Not the physical buffer array size) */
    protected int size;

    /** The wrapped input stream */
    protected OutputStream stream;

    /** The position in the stream */
    protected int position;

    /** The current byte */
    protected int currentByte;

    /** The current bit */
    protected byte currentBit = 0;


    /**
     * Constructor
     * 
     * @param stream
     *            The wrapped input stream
     */

    public SeekableOutputStream(OutputStream stream)
    {
        this.stream = stream;
        this.cache = new byte[32];
        this.position = 0;
        this.size = 0;
    }


    /**
     * @see java.io.OutputStream#write(int)
     */

    @Override
    public void write(int b)
    {
        if (this.position < this.size)
        {
            this.cache[this.position] = (byte) b;
        }
        else
        {
            int newSize = this.size + 1;
            if (newSize > this.cache.length)
            {
                byte newCache[] = new byte[Math.max(this.cache.length << 1,
                    newSize)];
                System.arraycopy(this.cache, 0, newCache, 0, this.size);
                this.cache = newCache;
            }
            this.cache[this.size] = (byte) b;
            this.size = newSize;
        }
        this.position++;
    }


    /**
     * Writes a bit to the output stream.
     * 
     * @param bit
     *            The bit to write
     */

    public void writeBit(byte bit)
    {
        writeBit(bit, false);
    }


    /**
     * Writes a bit to the output stream. This method can write the bits in
     * reversed order.
     * 
     * @param bit
     *            The bit to write
     * @param reverse
     *            If bits should be written in reversed order
     */

    public void writeBit(byte bit, boolean reverse)
    {
        if (reverse)
        {
            this.currentByte = this.currentByte
                | (((bit & 1) << this.currentBit));
        }
        else
        {
            this.currentByte = (this.currentByte << 1) | (bit & 1);
        }

        this.currentBit++;
        if (this.currentBit > 7)
        {
            write(this.currentByte);
            this.currentByte = 0;
            this.currentBit = 0;
        }
    }


    /**
     * Writes the specified number of bits. The bits can be written in reverse
     * order if the reverse flag is set.
     * 
     * @param value
     *            The value containing the bits to write
     * @param quantity
     *            The number of bits to write
     * @param reverse
     *            If the bits should be written reversed.
     */

    public void writeBits(int value, int quantity, boolean reverse)
    {
        byte b;

        for (int i = 0; i < quantity; i++)
        {
            if (reverse)
            {
                b = (byte) ((value >> i) & 1);
            }
            else
            {
                b = (byte) ((value >> (quantity - i - 1)) & 1);
            }
            writeBit(b, reverse);
        }
    }


    /**
     * Writes a bit to the output stream.
     * 
     * @param bit
     *            The bit to write
     */

    public void writeBit(boolean bit)
    {
        writeBit((byte) (bit ? 1 : 0));
    }


    /**
     * Writes a byte to the stream.
     * 
     * @param b
     *            The byte to write
     */

    public void writeByte(int b)
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
     */

    public void writeSignedByte(int b)
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
     */

    public void writeWord(int word)
    {
        writeByte(word & 0xff);
        writeByte((word >> 8) & 0xff);
    }


    /**
     * Writes a 4-byte integer to the stream.
     * 
     * @param integer
     *            The integer to write
     */

    public void writeInt(long integer)
    {
        writeByte((int) (integer & 0xff));
        writeByte((int) ((integer >> 8) & 0xff));
        writeByte((int) ((integer >> 16) & 0xff));
        writeByte((int) ((integer >> 24) & 0xff));
    }


    /**
     * Writes a 3-byte integer to the stream.
     * 
     * @param integer
     *            The integer to write
     */

    public void writeInt3(int integer)
    {
        writeByte(integer & 0xff);
        writeByte((integer >> 8) & 0xff);
        writeByte((integer >> 16) & 0xff);
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
        flush(false);
    }


    /**
     * Flush the output to make sure all bits are written even if they don't
     * fill a whole byte.
     * 
     * @param reverse
     *            In bits are written in reverese order
     * @throws IOException
     */

    public void flush(boolean reverse) throws IOException
    {
        if (this.currentBit != 0)
        {
            if (!reverse)
            {
                this.currentByte = this.currentByte << (8 - this.currentBit);
            }
            write(this.currentByte);
        }

        this.stream.write(this.cache, 0, this.size);
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


    /**
     * Returns the current position in the stream.
     * 
     * @return The current position in the stream
     */

    public long tell()
    {
        return this.position;
    }


    /**
     * Seeks to the specified position.
     * 
     * @param position
     *            The position to seek to
     */

    public void seek(long position)
    {
        this.currentByte = 0;
        this.currentBit = 0;
        if (this.position != position)
        {
            if (position < this.size)
            {
                this.position = (int) position;
            }
            else
            {
                long rest = position - this.size;
                this.position = this.size;
                skip(rest);
            }
        }
    }


    /**
     * Skips the specified amount of bytes by writing 0 bytes
     * 
     * @param bytes
     *            The number of bytes to skip
     */

    public void skip(long bytes)
    {
        for (int i = 0; i < bytes; i++)
        {
            writeByte(0);
        }
    }
}
