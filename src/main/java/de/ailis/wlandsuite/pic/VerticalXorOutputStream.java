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

package de.ailis.wlandsuite.pic;

import java.io.IOException;
import java.io.OutputStream;


/**
 * The VerticalXorOutputStream allows writing a vxor encoded data stream.
 * If this stream is closed then the connected output stream is also closed.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class VerticalXorOutputStream extends OutputStream
{
    /** The stream to write encoded data to */
    private final OutputStream stream;

    /** The width in pixels */
    private final int width;

    /** The last written line (used for encoding the next line) */
    private final int[] lastLine;

    /** Current X position in stream */
    private int x;

    /** Current Y position in stream */
    private int y;


    /**
     * Constructor
     *
     * @param stream
     *            The stream to write encoded data to
     * @param width
     *            The width in bytes (not in pixels)
     */

    public VerticalXorOutputStream(final OutputStream stream, final int width)
    {
        super();
        this.stream = stream;
        this.width = width / 2;
        this.lastLine = new int[width];
    }


    /**
     * @see java.io.OutputStream#write(int)
     */

    @Override
    public void write(final int b) throws IOException
    {
        // write unencoded byte for first row, encoded byte for all other rows
        if (this.y > 0)
        {
            this.stream.write(b ^ this.lastLine[this.x]);
        }
        else
        {
            this.stream.write(b);
        }

        // Remember the real byte for the next row
        this.lastLine[this.x] = b;

        // Move on the cursor
        if (this.x < this.width - 1)
        {
            this.x++;
        }
        else
        {
            this.y++;
            this.x = 0;
        }
    }


    /**
     * @throws IOException
     *             When file operation fails.
     * @see java.io.InputStream#close()
     */

    @Override
    public void close() throws IOException
    {
        if (this.stream != null)
        {
            this.stream.close();
        }
    }
}
