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

package de.ailis.wlandsuite.sprites;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ailis.wlandsuite.image.EgaImage;


/**
 * A sprite.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Sprite extends EgaImage
{
    /**
     * Constructor
     *
     * @param width
     *            The width
     * @param height
     *            The height
     */

    public Sprite(final int width, final int height)
    {
        super(width, height);
    }


    /**
     * Creates a sprite from a normal buffered image which is copied to the
     * picture. Colors are automatically converted to the EGA color palette.
     *
     * @param image
     *            The normal buffered image
     */

    public Sprite(final BufferedImage image)
    {
        super(image);
    }


    /**
     * Loads a sprite from a stream
     *
     * @param stream
     *            The input stream
     * @param width
     *            The width of the sprite to read in pixel
     * @param height
     *            The height of the sprite to read in pixel
     * @return The sprite
     * @throws IOException
     *             When file operation fails.
     */

    public static Sprite read(final InputStream stream, final int width, final int height)
        throws IOException
    {
        Sprite sprite;
        int b;
        int x, y;
        int bit, pixel;

        sprite = new Sprite(width, height);
        for (bit = 0; bit < 4; bit++)
        {
            for (y = 0; y < height; y++)
            {
                for (x = 0; x < width; x += 8)
                {
                    b = stream.read();
                    if (b == -1)
                    {
                        throw new EOFException(
                            "Unexcepted end of stream while reading picture");
                    }
                    for (pixel = 0; pixel < 8; pixel++)
                    {
                        sprite.setPixel(x + pixel, y, sprite.getPixel(
                            x + pixel, y)
                            | (((b >> (7 - pixel)) & 1) << bit));
                    }
                }
            }
        }
        return sprite;
    }


    /**
     * Writes the sprite to the specified output stream.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream) throws IOException
    {
        int b;
        int x, y;
        int bit, pixel;
        int width, height;

        width = getWidth();
        height = getHeight();
        for (bit = 0; bit < 4; bit++)
        {
            for (y = 0; y < height; y++)
            {
                for (x = 0; x < width; x += 8)
                {
                    b = 0;
                    for (pixel = 0; pixel < 8; pixel++)
                    {
                        b |= ((getPixel(x + pixel, y) >> bit) & 0x01) << (7 - pixel);
                    }
                    stream.write(b);
                }
            }
        }
    }
}
