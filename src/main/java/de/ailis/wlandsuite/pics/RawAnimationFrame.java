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

package de.ailis.wlandsuite.pics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import de.ailis.wlandsuite.pic.Pic;


/**
 * A raw animation frame represents the raw frame found in the wasteland
 * animation data block. It consists of multiple raw animation frame parts.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class RawAnimationFrame
{
    /** The raw animation frame parts */
    private List<RawAnimationFramePart> parts;

    /** The size of the animation frame */
    private int size;


    /**
     * Constructor
     * 
     * @param parts
     *            The animation frame parts
     * @param size
     *            The size of the animation frame
     */

    public RawAnimationFrame(List<RawAnimationFramePart> parts, int size)
    {
        this.parts = parts;
        this.size = size;
    }


    /**
     * Applies this animation frame to a image
     * 
     * @param image
     *            The image to update
     */

    public void apply(Pic image)
    {
        for (RawAnimationFramePart part: this.parts)
        {
            part.apply(image);
        }
    }


    /**
     * Reads the next raw animation frame from the input stream.
     * 
     * @param stream
     *            The input stream
     * @return The raw animation frame
     * @throws IOException
     */

    public static RawAnimationFrame read(InputStream stream) throws IOException
    {
        List<RawAnimationFramePart> parts;
        RawAnimationFramePart part;
        int size;

        parts = new ArrayList<RawAnimationFramePart>();
        size = 0;
        while ((part = RawAnimationFramePart.read(stream)) != null)
        {
            size += part.getSize();
            parts.add(part);
        }
        return new RawAnimationFrame(parts, size + 2);
    }


    /**
     * Writes the animation frame to the specified stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        // Wrtie the animation frame parts
        for (RawAnimationFramePart part: this.parts)
        {
            part.write(stream);
        }

        // Write the end of frame marker
        stream.write(0xff);
        stream.write(0xff);
    }


    /**
     * Returns the size.
     * 
     * @return The size
     */

    public int getSize()
    {
        return this.size;
    }


    /**
     * @see java.lang.Object#hashCode()
     */

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(this.parts).toHashCode();
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(Object o)
    {
        RawAnimationFrame other;

        if (o instanceof RawAnimationFrame == false)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        other = (RawAnimationFrame) o;
        return new EqualsBuilder().append(
            this.parts, other.parts).isEquals();
    }
}
