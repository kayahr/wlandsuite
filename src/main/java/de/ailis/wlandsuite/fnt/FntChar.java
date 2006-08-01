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

package de.ailis.wlandsuite.fnt;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ailis.wlandsuite.image.EgaImage;


/**
 * A font character.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class FntChar extends EgaImage
{
    /**
     * Constructor
     */

    public FntChar()
    {
        super(8, 8);
    }


    /**
     * Constructor
     * 
     * @param image
     *            The buffered image
     */

    public FntChar(BufferedImage image)
    {
        super(image);
    }


    /**
     * Reads a font character from a stream
     * 
     * @param stream
     *            The input stream
     * @return The cursor
     * @throws IOException
     */

    public static FntChar read(InputStream stream) throws IOException
    {
        FntChar fntChar;
        int b;
        int y;
        int bit, pixel;

        fntChar = new FntChar();
        for (bit = 0; bit < 4; bit++)
        {
            for (y = 0; y < 8; y++)
            {
                b = stream.read();
                if (b == -1)
                {
                    throw new EOFException(
                        "Unexcepted end of stream while reading picture");
                }
                for (pixel = 0; pixel < 8; pixel++)
                {
                    fntChar.setPixel(pixel, y, fntChar.getPixel(pixel, y)
                        | (((b >> (7 - pixel)) & 1) << bit));
                }
            }
        }
        return fntChar;
    }


    /**
     * Writes the character to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        int b;
        int y;
        int bit, pixel;

        for (bit = 0; bit < 4; bit++)
        {
            for (y = 0; y < 8; y++)
            {
                b = 0;
                for (pixel = 0; pixel < 8; pixel++)
                {
                    b |= ((getPixel(pixel, y) >> bit) & 0x01) << (7 - pixel);
                }
                stream.write(b);
            }
        }
    }
}
