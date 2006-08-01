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
import java.awt.image.IndexColorModel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * A palette image.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class PaletteImage extends BufferedImage
{
    /**
     * Constructor
     * 
     * @param width
     *            The width
     * @param height
     *            The height
     * @param imageType
     *            The image type
     * @param cm
     *            The color model
     */

    public PaletteImage(int width, int height, int imageType, IndexColorModel cm)
    {
        super(width, height, imageType, cm);
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
        for (int i = 0; i < getPalette().length; i++)
        {
            if (getPalette()[i] == color) return i;
        }
        return -1;
    }


    /**
     * Returns the palette.
     * 
     * @return The palette
     */

    protected abstract int[] getPalette();


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
        setRGB(x, y, getPalette()[color]);
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
     * @see java.lang.Object#hashCode()
     */

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(getPixels()).toHashCode();
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(Object o)
    {
        PaletteImage other;
        
        if (o instanceof PaletteImage == false)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        other = (PaletteImage) o;
        return new EqualsBuilder().append(getPixels(), other.getPixels())
            .isEquals();
    }


    /**
     * Returns the pixels of the image.
     * 
     * @return The image pixels
     */

    private int[] getPixels()
    {
        int[] pixels;
        int w, h, x, y;

        w = getWidth();
        h = getHeight();
        pixels = new int[w * h];
        for (y = 0; y < h; y++)
        {
            for (x = 0; x < w; x++)
            {
                pixels[y * w + x] = getRGB(x, y);
            }
        }
        return pixels;
    }
}
