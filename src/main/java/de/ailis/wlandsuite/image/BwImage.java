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
 * An black/white image.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class BwImage extends PaletteImage
{
    /** The color palette */
    private static final int palette[] = { 0xff000000, 0xffffffff }; 


    /**
     * Constructor
     * 
     * @param width
     *            The picture width
     * @param height
     *            The picture height
     */

    public BwImage(int width, int height)
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

    public BwImage(BufferedImage image)
    {
        this(image.getWidth(), image.getHeight());
        createGraphics().drawImage(image, 0, 0, null);
    }


    /**
     * Reads an image from the specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The image
     * @throws IOException
     */

    public static BwImage read(InputStream stream) throws IOException
    {
        BufferedImage image;

        image = ImageIO.read(stream);
        if (image == null)
        {
            throw new IOException("Unable to read image from stream");
        }
        return new BwImage(image);
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
