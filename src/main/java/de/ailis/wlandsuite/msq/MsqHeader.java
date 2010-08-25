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

package de.ailis.wlandsuite.msq;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * MSQ header
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MsqHeader implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -5186751829710381419L;

    /** The MSQ block type */
    private final MsqType type;

    /** The disk index (0 or 1) */
    private final int disk;

    /** The size of the uncompressed MSQ data */
    private final int size;


    /**
     * Constructor
     *
     * @param type
     *            The MSQ block type
     * @param disk
     *            The disk index (0 or 1)
     * @param size
     *            The size of the uncompressed MSQ data
     */

    public MsqHeader(final MsqType type, final int disk, final int size)
    {
        this.type = type;
        this.disk = disk;
        this.size = size;
    }


    /**
     * Reads a MSQ header from the stream. If the end of the stream has been
     * reached then NULL is returned.
     *
     * @param stream
     *            The input stream
     * @return The MSQ header or null if end of stream
     * @throws IOException
     *             When file operation fails.
     */

    public static MsqHeader read(final InputStream stream) throws IOException
    {
        int b1, b2, b3, b4;
        int size;

        // Read the next four bytes. If the first byte hit the end of the
        // stream then no more MSQ blocks are available and null is returned
        b1 = stream.read();
        if (b1 == -1)
        {
            return null;
        }
        b2 = stream.read();
        b3 = stream.read();
        b4 = stream.read();
        if (b2 == -1 || b3 == -1 || b4 == -1)
        {
            throw new EOFException(
                "Unexpected end of stream while reading MSQ header");
        }

        // Check for uncompressed MSQ block type
        if (b1 == 'm' && b2 == 's' && b3 == 'q' && (b4 == '0' || b4 == '1'))
        {
            return new MsqHeader(MsqType.Uncompressed, b4 - '0', 0);
        }

        // Assume the first four bytes are size information and read the next
        // four bytes
        size = b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
        b1 = stream.read();
        b2 = stream.read();
        b3 = stream.read();
        b4 = stream.read();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1)
        {
            throw new EOFException(
                "Unexpected end of stream while reading MSQ header");
        }

        // Check for compressed MSQ block type
        if (b1 == 'm' && b2 == 's' && b3 == 'q' && (b4 == 0 || b4 == 1))
        {
            return new MsqHeader(MsqType.Compressed, b4, size);
        }

        // Check for CPA Animation block type
        if (b1 == 0x08 && b2 == 0x67 && b3 == 0x01 && b4 == 0)
        {
            return new MsqHeader(MsqType.CpaAnimation, b4, size);
        }

        // Give up, unknown MSQ block type
        throw new IOException("Unable to read MSQ header from stream");
    }


    /**
     * Writes the MSQ header to the given output stream
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream) throws IOException
    {
        // Write the size information for compressed or Cpa animation block
        if (this.type == MsqType.Compressed || this.type == MsqType.CpaAnimation)
        {
            stream.write(this.size & 0xff);
            stream.write((this.size >> 8) & 0xff);
            stream.write((this.size >> 16) & 0xff);
            stream.write((this.size >> 24) & 0xff);
        }

        // Write the MSQ identifier
        if (this.type == MsqType.CpaAnimation)
        {
            stream.write(0x08);
            stream.write(0x67);
            stream.write(0x01);
        }
        else
        {
            stream.write('m');
            stream.write('s');
            stream.write('q');
        }

        // Write the disk index
        if (this.type == MsqType.Uncompressed)
        {
            stream.write(this.disk + '0');
        }
        else
        {
            stream.write(this.disk);
        }
    }


    /**
     * Returns the disk index (0 or 1).
     *
     * @return The disk index
     */

    public int getDisk()
    {
        return this.disk;
    }


    /**
     * Returns the size of the uncompressed MSQ block data.
     *
     * @return The size of the uncompressed data
     */

    public int getSize()
    {
        return this.size;
    }


    /**
     * Returns the MSQ block type.
     *
     * @return The MSQ block type
     */

    public MsqType getType()
    {
        return this.type;
    }
}
