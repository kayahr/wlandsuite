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

package de.ailis.wlandsuite.curs;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ailis.wlandsuite.image.EgaImage;


/**
 * A cursor image.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Cursor
{
    /** The cursor image */
    private EgaImage cursor;

    /** The mask image */
    private EgaImage mask;


    /**
     * Constructor
     * 
     * @param cursor
     *            The cursor image
     * @param mask
     *            The mask image
     */

    public Cursor(EgaImage cursor, EgaImage mask)
    {
        this.cursor = cursor;
        this.mask = mask;
    }
    

    /**
     * Loads a cursor from a stream
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of the cursor to read in pixel
     * @param height
     *            The height of the cursor to read in pixel
     * @return The cursor
     * @throws IOException
     */

    public static Cursor read(InputStream stream, int width, int height)
        throws IOException
    {
        EgaImage cursor, mask, image;
        int b;
        int x, y;
        int type, bit, pixel;

        cursor = new EgaImage(width, height);
        mask = new EgaImage(width, height);
        for (bit = 0; bit < 4; bit++)
        {
            for (y = 0; y < height; y++)
            {
                for (type = 0; type < 2; type++)
                {
                    image = type == 0 ? mask : cursor;
                    for (x = width - 8; x >= 0; x -= 8)
                    {
                        b = stream.read();
                        if (b == -1)
                        {
                            throw new EOFException(
                                "Unexcepted end of stream while reading picture");
                        }
                        for (pixel = 0; pixel < 8; pixel++)
                        {
                            image.setPixel(x + pixel, y, image.getPixel(x
                                + pixel, y)
                                | (((b >> (7 - pixel)) & 1) << bit));
                        }
                    }
                }
            }
        }
        return new Cursor(cursor, mask);
    }


    /**
     * Writes the picture to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        EgaImage image;
        int b;
        int x, y;
        int type, bit, pixel;
        int width, height;

        width = this.cursor.getWidth();
        height = this.cursor.getHeight();
        for (bit = 0; bit < 4; bit++)
        {
            for (y = 0; y < height; y++)
            {
                for (type = 0; type < 2; type++)
                {
                    image = type == 0 ? this.mask : this.cursor;
                    for (x = width - 8; x >= 0; x -= 8)
                    {
                        b = 0;
                        for (pixel = 0; pixel < 8; pixel++)
                        {
                            b |= ((image.getPixel(x + pixel, y) >> bit) & 0x01) << (7 - pixel);
                        }
                        stream.write(b);
                    }
                }
            }
        }
    }


    /**
     * Returns the cursor image.
     * 
     * @return The cursor image
     */

    public EgaImage getCursor()
    {
        return this.cursor;
    }


    /**
     * Returns the mask image.
     * 
     * @return The mask image
     */

    public EgaImage getMask()
    {
        return this.mask;
    }
}
