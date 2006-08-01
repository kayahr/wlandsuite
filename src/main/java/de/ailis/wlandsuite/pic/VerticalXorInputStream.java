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
import java.io.InputStream;


/**
 * The VerticalXorInputStream allows reading a vxor encoded data stream.
 * If this stream is closed then the connected input stream is also closed.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class VerticalXorInputStream extends InputStream
{
    /** The stream to read encoded data from */
    private InputStream stream;

    /** The width in bytes (not in pixels) */
    private int width;
    
    /** The last read line (used for decoding the next line) */
    private int[] lastLine;
    
    /** Current X position in stream */
    private int x;
    
    /** Current Y position in stream */
    private int y;


    /**
     * Constructor
     * 
     * @param stream
     *            The stream to read encoded data from
     * @param width
     *            The width in pixels
     */

    public VerticalXorInputStream(InputStream stream, int width)
    {
        super();
        this.stream = stream;
        this.width = width / 2;
        this.lastLine = new int[width];
    }


    /**
     * @see java.io.InputStream#read()
     */

    @Override
    public int read() throws IOException
    {
        int b;
        
        // Read encoded byte from stream        
        b = this.stream.read();
        if (b == -1)
        {
            return -1;
        }
        
        // Decode the byte it it's not in the first (unencoded) row
        if (this.y > 0)
        {
            b = b ^ this.lastLine[this.x];
        }
        
        // Remember the decoded byte for the next row
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
        
        // Return the decoded byte
        return b;
    }
    
    
    /**
     * @throws IOException 
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
