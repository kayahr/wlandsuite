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

package de.ailis.wlandsuite.cpa;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.pic.Pic;


/**
 * A CPA frame represents an animation frame in a CPA file. It consists of a
 * picture and a delay which is used to wait some time before the frame is
 * displayed.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CpaFrame implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -3999239605292452602L;

    /** The delay */
    private int delay;

    /** The picture */
    private Pic pic;


    /**
     * Constructor
     * 
     * @param delay
     *            The delay
     * @param pic
     *            The picture
     */

    public CpaFrame(int delay, Pic pic)
    {
        this.delay = delay;
        this.pic = pic;
    }
    
    
    /**
     * Private empty constructor
     */
    
    private CpaFrame()
    {
        super();
    }


    /**
     * Reads the next CPA frame from the given input stream. If the end of the
     * animation has been reached then null is returned.
     * 
     * @param stream
     *            The input stream
     * @param basePic
     *            The base pic for this frame
     * @return The CPA frame or null if end of animation has been reached
     * @throws IOException
     */

    public static CpaFrame read(InputStream stream, Pic basePic)
        throws IOException
    {
        CpaFrame frame;
        BitInputStream dataStream;
        int offset;
        int delay;
        int i;
        int b;
        int x, y;

        // Wrap the stream into a DataInputStream for easier access
        dataStream = new BitInputStreamWrapper(stream);

        // Read the delay. 0xffff is end of animation
        delay = dataStream.readWord();
        if (delay == -1)
        {
            throw new EOFException(
                "Unexpected end of stream while reading CPA frame delay");
        }
        if (delay == 0xffff)
        {
            return null;
        }

        // Create a new CPA frame
        frame = new CpaFrame();
        frame.delay = delay;
        frame.pic = basePic.clone();

        // Cycle over all animation frame parts
        while (true)
        {
            // Read the offset. 0xffff means end of frame
            offset = dataStream.readWord();
            if (offset == -1)
            {
                throw new EOFException(
                    "Unexpected end of stream while reading CPA frame offset");
            }
            if (offset == 0xffff)
            {
                return frame;
            }

            // Calculate the X and Y position
            y = (offset * 8) / 320;
            x = (offset * 8) % 320;

            // Read the four update bytes and apply them to the picture
            for (i = 0; i < 4; i++)
            {
                b = dataStream.readByte();
                if (b == -1)
                {
                    throw new EOFException(
                        "Unexpected end of stream while reading CPA frame part");
                }
                frame.pic.setPixel(x + i * 2, y, b >> 4);
                frame.pic.setPixel(x + i * 2 + 1, y, b & 0xf);
            }
        }
    }


    /**
     * Returns the delay.
     * 
     * @return The delay
     */

    public int getDelay()
    {
        return this.delay;
    }


    /**
     * Returns the pic.
     * 
     * @return The pic
     */

    public Pic getPic()
    {
        return this.pic;
    }
}
