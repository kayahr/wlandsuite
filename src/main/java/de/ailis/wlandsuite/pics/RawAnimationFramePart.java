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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import de.ailis.wlandsuite.pic.Pic;


/**
 * A raw animation frame part defines a starting position in the base frame and
 * a number of byte diffs (palette XORs).
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class RawAnimationFramePart
{
    /** The starting offset */
    private final int offset;

    /** The diff (palette XORs) */
    private int[] diff;

    /** The frame part size */
    private final int size;


    /**
     * Constructor
     *
     * @param offset
     *            The starting offset
     * @param diff
     *            The diff (palette XORs)
     * @param size
     *            The size
     */

    public RawAnimationFramePart(final int offset, final int[] diff, final int size)
    {
        this.offset = offset;
        this.diff = diff;
        this.size = size;
    }


    /**
     * Applies this animation frame part to a image.
     *
     * @param image
     *            The image to modify
     */

    public void apply(final Pic image)
    {
        int x, y;
        int xor;

        x = (this.offset * 2) % image.getWidth();
        y = (this.offset * 2) / image.getWidth();
        for (final int xors: this.diff)
        {
            xor = (xors >> 4) & 0x0f;
            image.setPixel(x, y, image.getPixel(x, y) ^ xor);
            x++;
            if (x >= image.getWidth())
            {
                x = 0;
                y++;
            }
            xor = xors & 0x0f;
            image.setPixel(x, y, image.getPixel(x, y) ^ xor);
            x++;
            if (x >= image.getWidth())
            {
                x = 0;
                y++;
            }
        }
    }


    /**
     * Parses the next animation frame part from the specified input stream. If
     * the end of the animation frame has been reached then this method returns
     * null
     *
     * @param stream
     *            The input stream
     * @return The next animation frame part or null if end of frame reached
     * @throws IOException
     *             When file operation fails.
     */

    public static RawAnimationFramePart read(final InputStream stream)
        throws IOException
    {
        int address;
        int bytes;
        int h, l;
        int offset;
        int size;
        int[] diff;

        // Read address from stream. Abort if hit the end of the frame part
        l = stream.read();
        h = stream.read();
        if (l == -1 || h == -1)
        {
            throw new IOException("Unexpected end of frame part stream");
        }
        address = l | (h << 8);
        if (address == 0xffff)
        {
            return null;
        }

        // Extract number of diff bytes from address
        bytes = ((address >> 12) & 0xf) + 1;
        address = address & 0xfff;

        size = 2;
        offset = address;
        diff = new int[bytes];
        for (int i = 0; i < bytes; i++)
        {
            diff[i] = stream.read();
            if (diff[i] == -1)
            {
                throw new IOException("Unexpected end of frame part stream");
            }
            size++;
        }
        return new RawAnimationFramePart(offset, diff, size);
    }


    /**
     * Writes this animation frame part to the specified stream.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream) throws IOException
    {
        int address;

        // Write address to stream
        address = ((this.diff.length - 1) << 12) | this.offset;
        stream.write(address & 0xff);
        stream.write((address >> 8) & 0xff);

        // Write the update bytes
        for (final int b: this.diff)
        {
            stream.write(b);
        }
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
        return new HashCodeBuilder(17, 37).append(this.offset)
            .append(this.diff).toHashCode();
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(final Object o)
    {
        RawAnimationFramePart other;

        if (o instanceof RawAnimationFramePart == false)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        other = (RawAnimationFramePart) o;
        return new EqualsBuilder().append(this.offset, other.offset).append(
            this.diff, other.diff).isEquals();
    }


    /**
     * Checks if this frame part is mergable with the specified animation frame
     * part
     *
     * @param other
     *            The other animation frame part
     * @return If the frames are mergable or not
     */

    public boolean isMergable(final RawAnimationFramePart other)
    {
        int size;

        size = other.offset - this.offset + other.diff.length;
        if (size > 16)
        {
            return false;
        }
        if ((size + 2) >= (other.diff.length + this.diff.length + 4))
        {
            return false;
        }
        return true;
    }


    /**
     * Merges this frame part with an other frame part
     *
     * @param other
     *            The other frame part
     */

    public void merge(final RawAnimationFramePart other)
    {
        int nulls;
        int[] newDiff;

        nulls = other.offset - this.offset - this.diff.length;
        newDiff = new int[nulls + this.diff.length + other.diff.length];
        for (int i = 0; i < this.diff.length; i++)
        {
            newDiff[i] = this.diff[i];
        }
        for (int i = 0; i < nulls; i++)
        {
            newDiff[this.diff.length + i] = 0;
        }
        for (int i = 0; i < other.diff.length; i++)
        {
            newDiff[this.diff.length + nulls + i] = other.diff[i];
        }
        this.diff = newDiff;
    }
}
