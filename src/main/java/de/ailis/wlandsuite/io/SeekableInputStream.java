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
import java.io.InputStream;


/**
 * This InputStream implementation wraps another InputStream and provides a byte
 * cache which allows seeking in the stream. Be careful! If you are reading lots
 * of data with this Class then you may have a large memory footprint because
 * all the data you read is cached in a byte array.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class SeekableInputStream extends InputStream
{
    /** The internal byte cache */
    protected byte cache[];

    /** The logical buffer size (Not the physical buffer array size) */
    protected int size;

    /** The underlying stream */
    protected InputStream stream;

    /** The position in the stream */
    protected int position;

    /** The current byte */
    protected int currentByte;

    /** The current bit */
    protected byte currentBit = 7;


    /**
     * Constructor
     * 
     * @param stream
     */

    public SeekableInputStream(InputStream stream)
    {
        this.stream = stream;
        this.cache = new byte[32];
        this.position = 0;
        this.size = 0;
    }


    /**
     * @see java.io.InputStream#read()
     */

    @Override
    public int read() throws IOException
    {
        int b;

        if (this.position < this.size)
        {
            b = this.cache[this.position] & 0xff;
        }
        else
        {
            b = readReal();
            if (b == -1) return -1;
        }
        this.position++;
        return b;
    }


    /**
     * Reads a byte from the underlying input stream. The byte is also cached
     * and the cache is enlarged if needed.
     * 
     * @return The next byte in the underlying input stream
     * @throws IOException
     */

    protected int readReal() throws IOException
    {
        int b;

        b = this.stream.read();
        if (b == -1) return -1;
        int newSize = this.size + 1;
        if (newSize > this.cache.length)
        {
            byte newCache[] = new byte[Math
                .max(this.cache.length << 1, newSize)];
            System.arraycopy(this.cache, 0, newCache, 0, this.size);
            this.cache = newCache;
        }
        this.cache[this.size] = (byte) b;
        this.size = newSize;
        return b;
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
     * @throws IOException
     */

    public void seek(long position) throws IOException
    {
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
        this.currentByte = 0;
        this.currentBit = 7;
    }


    /**
     * Reads a bit from the input stream and returns it (0 or 1). Returns -1 if
     * there is no more data on the stream.
     * 
     * @return The next bit in the stream
     * @throws IOException
     */

    public byte readBit() throws IOException
    {
        return readBit(false);
    }


    /**
     * Reads a bit from the input stream and returns it (0 or 1). Returns -1 if
     * there is no more data on the stream. This method can read the bits in
     * reversed order.
     * 
     * @param reverse
     *            If bits should be read in reversed order
     * @return The next bit in the stream
     * @throws IOException
     */

    public byte readBit(boolean reverse) throws IOException
    {
        if (this.currentBit > 6)
        {
            this.currentByte = read();
            if (this.currentByte == -1)
            {
                return -1;
            }
            this.currentBit = 0;
        }
        else
        {
            this.currentBit++;
        }

        if (reverse)
        {
            return (byte) ((this.currentByte >> (this.currentBit)) & 1);
        }
        else
        {
            return (byte) ((this.currentByte >> (7 - this.currentBit)) & 1);
        }
    }


    /**
     * Reads the specified number of bits. The bits can be read in reverse order
     * if the reverse flag is set.
     * 
     * @param quantity
     *            The number of bits to read
     * @param reverse
     *            If the bits should be read reversed.
     * @return The bits
     * @throws IOException
     */

    public int readBits(int quantity, boolean reverse) throws IOException
    {
        int value;
        byte b;

        value = 0;
        for (int i = 0; i < quantity; i++)
        {
            b = readBit(reverse);
            if (b == -1)
            {
                return -1;
            }
            if (reverse)
            {
                value = value | (b << i);
            }
            else
            {
                value = (value << 1) | b;
            }
        }
        return value;
    }


    /**
     * Reads a byte from the stream. The difference to read() is that the byte
     * must not be byte-aligned in the stream. So if you read 4 bits and then a
     * byte then this byte contains the last 4 bit of the current byte and the
     * next 4 bit of the next byte.
     * 
     * @return A byte
     * @throws IOException
     */

    public int readByte() throws IOException
    {
        int b;
        byte bit;

        // If we are at a full byte align then read the byte right away
        if (this.currentBit == 7)
        {
            return read();
        }

        // Otherwise read 8 bits and construct a byte from it
        b = 0;
        for (int i = 0; i < 8; i++)
        {
            bit = readBit();
            if (bit == -1)
            {
                return -1;
            }
            b = b << 1 | bit;
        }
        return b;
    }


    /**
     * Reads a signed byte.
     * 
     * @return The signed byte
     * @throws IOException
     */

    public int readSignedByte() throws IOException
    {
        int b;

        b = readByte();
        if (b >= 128)
        {
            return b - 256;
        }
        else
        {
            return b;
        }
    }


    /**
     * Reads a 4-byte integer from the stream. Returns -1 if the end of the
     * stream has been reached. The method don't need a byte-aligned stream. So
     * if you have read 4 bits from the stream the the next 32 bits are the int
     * which is read by this method.
     * 
     * @return The integer value
     * @throws IOException
     */

    public long readInt() throws IOException
    {
        int b1, b2, b3, b4;

        b1 = readByte();
        b2 = readByte();
        b3 = readByte();
        b4 = readByte();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1)
        {
            return -1;
        }
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
    }


    /**
     * Reads a 4-byte signed integer from the stream. Returns -1 if the end of
     * the stream has been reached. The method don't need a byte-aligned stream.
     * So if you have read 4 bits from the stream the the next 32 bits are the
     * int which is read by this method.
     * 
     * @return The integer value
     * @throws IOException
     */

    public int readSignedInt() throws IOException
    {
        int b1, b2, b3, b4;

        b1 = readByte();
        b2 = readByte();
        b3 = readByte();
        b4 = readByte();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1)
        {
            return -1;
        }
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
    }


    /**
     * Reads a 3-byte integer from the stream. Returns -1 if the end of the
     * stream has been reached. The method don't need a byte-aligned stream. So
     * if you have read 4 bits from the stream the the next 24 bits are the int
     * which is read by this method.
     * 
     * @return The integer value
     * @throws IOException
     */

    public int readInt3() throws IOException
    {
        int b1, b2, b3;

        b1 = readByte();
        b2 = readByte();
        b3 = readByte();
        if (b1 == -1 || b2 == -1 || b3 == -1)
        {
            return -1;
        }
        return b1 | (b2 << 8) | (b3 << 16);
    }


    /**
     * Reads a 2-byte word from the stream. Returns -1 if the end of the stream
     * has been reached. The method don't need a byte-aligned stream. So if you
     * have read 4 bits from the stream the the next 16 bits are the word which
     * is read by this method.
     * 
     * @return The word value
     * @throws IOException
     */

    public int readWord() throws IOException
    {
        int b1, b2;

        b1 = readByte();
        b2 = readByte();
        if (b1 == -1 || b2 == -1)
        {
            return -1;
        }
        return b1 | (b2 << 8);
    }
}
