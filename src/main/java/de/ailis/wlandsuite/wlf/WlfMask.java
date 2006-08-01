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

package de.ailis.wlandsuite.wlf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ailis.wlandsuite.image.BwImage;


/**
 * WLF mask.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class WlfMask extends BwImage implements Cloneable
{
    /**
     * Constructor
     * 
     * @param width
     *            The mask width
     * @param height
     *            The mask height
     */

    public WlfMask(int width, int height)
    {
        super(width, height);
    }


    /**
     * Creates a mask from a normal buffered image which is copied to the
     * picture. Colors are automatically converted to the black and white color
     * palette of the mask.
     * 
     * @param image
     *            The normal buffered image
     */

    public WlfMask(BufferedImage image)
    {
        super(image);
    }    


    /**
     * Reads a mask from a file. A size of 16x16 is assumed which is the
     * standard size of Wasteland's bit masks. If you want to read a mask with
     * different dimensions then use the read method where you can specify a
     * custom size.
     * 
     * @param file
     *            The file to read
     * @return The mask
     * @throws IOException
     * 
     * @see WlfMask#read(File, int, int)
     */

    public static WlfMask read(File file) throws IOException
    {
        return read(file, 16, 16);
    }


    /**
     * Reads a mask from a file. The width and height must be specified because
     * the image dimensions can't be read from the file.
     * 
     * @param file
     *            The file to read
     * @param width
     *            The mask width
     * @param height
     *            The mask width
     * @return The mask
     * @throws IOException
     */

    public static WlfMask read(File file, int width, int height) throws IOException
    {
        InputStream stream;

        stream = new FileInputStream(file);
        try
        {
            return read(stream, width, height);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Loads a mask from a stream.
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of the mask to read in pixel
     * @param height
     *            The height of the mask to read in pixel
     * @return The picture
     * @throws IOException
     */

    public static WlfMask read(InputStream stream, int width, int height)
        throws IOException
    {
        WlfMask mask;
        int b;
        int x, y, bit;

        mask = new WlfMask(width, height);
        for (y = 0; y < height; y++)
        {
            for (x = 0; x < width; x += 8)
            {
                b = stream.read();
                if (b == -1)
                {
                    throw new EOFException(
                        "Unexcepted end of stream while reading mask");
                }
                for (bit = 0; bit < 8; bit++)
                {
                    mask.setPixel(x + bit, y, (b >> (7 - bit)) & 0x01);
                }
            }
        }
        return mask;
    }


    /**
     * Writes the mask to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        int width, height;
        int x, y, bit;
        int b;

        width = getWidth();
        height = getHeight();
        for (y = 0; y < height; y++)
        {
            for (x = 0; x < width; x += 8)
            {
                b = 0;
                for (bit = 0; bit < 8; bit ++)
                {
                    b |= getPixel(x + bit, y) << (7 - bit);
                }
                stream.write(b);
            }
        }
    }


    /**
     * Returns the bytes of the mask.
     * 
     * @return The mask bytes
     */

    public byte[] getBytes()
    {
        ByteArrayOutputStream stream;

        stream = new ByteArrayOutputStream();
        try
        {
            try
            {
                write(stream);
                return stream.toByteArray();
            }
            finally
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            // Ignored. Can't happen
            return null;
        }
    }


    /**
     * Writes the mask to a file
     * 
     * @param file
     *            The file
     * @throws IOException
     */

    public void write(File file) throws IOException
    {
        OutputStream stream;

        stream = new FileOutputStream(file);
        try
        {
            write(stream);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Returns a copy of the mask.
     * 
     * @return The copy of the source mask
     */

    @Override
    public WlfMask clone()
    {
        WlfMask dest;

        dest = new WlfMask(getWidth(), getHeight());
        dest.createGraphics().drawImage(this, 0, 0, null);
        return dest;
    }
}
