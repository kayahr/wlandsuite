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

package de.ailis.wlandsuite.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


/**
 * An EGA image. It's the base for classes Pic and Cursor.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class EgaImage extends BufferedImage
{
    /** The color palette */
    private static final int palette[] = { 0xff000000, 0xff0000aa, 0xff00aa00,
        0xff00aaaa, 0xffaa0000, 0xffaa00aa, 0xffaa5500, 0xffaaaaaa, 0xff555555,
        0xff5555ff, 0xff55ff55, 0xff55ffff, 0xffff5555, 0xffff55ff, 0xffffff55,
        0xffffffff };


    /**
     * Constructor
     * 
     * @param width
     *            The picture width
     * @param height
     *            The picture height
     */

    public EgaImage(int width, int height)
    {
        super(width, height, TYPE_BYTE_BINARY, new IndexColorModel(4,
            palette.length, palette, 0, false, -1, DataBuffer.TYPE_BYTE));
    }


    /**
     * Creates a picture from a normal buffered image which is copied to the
     * picture. Colors are automatically converted to the 16 color palette of
     * the picture.
     * 
     * @param image
     *            The normal buffered image
     */

    public EgaImage(BufferedImage image)
    {
        this(image.getWidth(), image.getHeight());
        createGraphics().drawImage(image, 0, 0, null);
    }


    /**
     * Returns the palette index for the specified color. Returns -1 if the
     * color was not found in the palette.
     * 
     * @param color
     *            The color
     * @return The palette index
     */

    private int getPaletteIndex(int color)
    {
        for (int i = 0; i < palette.length; i++)
        {
            if (palette[i] == color) return i;
        }
        return -1;
    }


    /**
     * Sets a pixel. The difference to setRGB is that the color is the palette
     * index (0-15) and not the RGB value.
     * 
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     * @param color
     *            The color index (0-15)
     */

    public void setPixel(int x, int y, int color)
    {
        setRGB(x, y, palette[color]);
    }


    /**
     * Returns a pixel color. The difference to getRGB is that the color is the
     * palette index (0-15) and not the RGB value.
     * 
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     * @return The color index (0-15) of the pixel
     */

    public int getPixel(int x, int y)
    {
        return getPaletteIndex(getRGB(x, y));
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(Object o)
    {
        EgaImage other;
        int w, h, x, y;

        try
        {
            other = (EgaImage) o;
        }
        catch (ClassCastException e)
        {
            return false;
        }
        if (other == null)
        {
            return false;
        }

        w = getWidth();
        h = getHeight();
        if (other.getWidth() == w && other.getHeight() == h)
        {
            for (y = 0; y < h; y++)
            {
                for (x = 0; x < w; x++)
                {
                    if (getRGB(x, y) != other.getRGB(x, y))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }


    /**
     * Reads an image from the specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The image
     * @throws IOException
     */

    public static EgaImage read(InputStream stream) throws IOException
    {
        BufferedImage image;

        image = ImageIO.read(stream);
        if (image == null)
        {
            throw new IOException("Unable to read image from stream");
        }
        return new EgaImage(image);
    }
}
