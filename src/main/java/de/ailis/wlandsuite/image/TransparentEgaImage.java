/*
 * $Id: EgaImage.java 67 2006-08-01 20:25:34Z k $
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;


/**
 * A transparent EGA image. It has the standard 16 color palette with an
 * additional color at index 16 which indicates a transparent color.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision: 67 $
 */

public class TransparentEgaImage extends PaletteImage
{
    /** The color palette */
    private static final int palette[] = { 0x89abcdef, 0xff000000, 0xff0000aa,
        0xff00aa00, 0xff00aaaa, 0xffaa0000, 0xffaa00aa, 0xffaa5500, 0xffaaaaaa,
        0xff555555, 0xff5555ff, 0xff55ff55, 0xff55ffff, 0xffff5555, 0xffff55ff,
        0xffffff55, 0xffffffff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0 };


    /**
     * Constructor
     * 
     * @param width
     *            The picture width
     * @param height
     *            The picture height
     */

    public TransparentEgaImage(final int width, final int height)
    {
        this(width, height, palette);
    }


    /**
     * Constructor
     * 
     * @param width
     *            The picture width
     * @param height
     *            The picture height
     * @param palette
     *            The palette to use. First color is always used for
     *            transparency
     */

    public TransparentEgaImage(final int width, final int height,
        final int[] palette)
    {
        super(width, height, TYPE_BYTE_INDEXED, new IndexColorModel(8,
            palette.length, palette, 0, false, 0, DataBuffer.TYPE_BYTE));
    }


    /**
     * Creates a picture from a normal buffered image which is copied to the
     * picture. Colors are automatically converted to the 16 color palette of
     * the picture.
     * 
     * @param image
     *            The normal buffered image
     */

    public TransparentEgaImage(final BufferedImage image)
    {
        this(image.getWidth(), image.getHeight()/*, createPalette(image)*/);
        final Graphics2D ctx = createGraphics();
        ctx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ctx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        ctx.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);            
        ctx.drawImage(image, 0, 0, null);
    }

    static class CountColor implements Comparable<CountColor>
    {
        public int color;

        public int counter;

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */

        public int compareTo(final CountColor o)
        {
            if (this.counter < o.counter) return 1;
            if (this.counter > o.counter) return -1;
            return 0;
        }
        
        @Override
        public String toString()
        {
            return Long.toString(color & 0xffffffffL, 16);
        }
    }

    private static int[] createPalette(final BufferedImage image)
    {
        final int palette[] = new int[256];
        final Map<Integer, CountColor> colors = new HashMap<Integer, CountColor>();
        final int iw = image.getWidth();
        final int ih = image.getHeight();
        for (int y = 0; y < ih; y++)
        {
            for (int x = 0; x < iw; x++)
            {
                final int color = image.getRGB(x, y);
                CountColor countColor = colors.get(color);
                if (countColor == null)
                {
                    countColor = new CountColor();
                    countColor.color = color;
                    countColor.counter = 1;
                    colors.put(color, countColor);
                } else countColor.counter++;
            }
        }
        final ArrayList<CountColor> list = new ArrayList<CountColor>(colors.values());
        Collections.sort(list);

        palette[0] = 0x89abcdef;
        final int maxColors = Math.min(254, list.size());
        for (int i = 0; i < maxColors; i++)
        {
            palette[i + 1] = list.get(i).color;
        }
        for (int i = maxColors; i < 255; i++) palette[i + 1] = 0;
        return palette;
    }

    /**
     * Reads an image from the specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The image
     * @throws IOException
     */

    public static TransparentEgaImage read(final InputStream stream)
        throws IOException
    {
        BufferedImage image;

        image = ImageIO.read(stream);
        if (image == null)
        {
            throw new IOException("Unable to read image from stream");
        }
        return new TransparentEgaImage(image);
    }


    /**
     * @see de.ailis.wlandsuite.image.PaletteImage#getPalette()
     */

    @Override
    protected int[] getPalette()
    {
        return palette;
    }
}
