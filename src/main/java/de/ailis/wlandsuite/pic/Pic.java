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

package de.ailis.wlandsuite.pic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.image.EgaImage;


/**
 * A PIC (most likely a "Picture") is a simple vertical XOR encoded picture.
 * This format is used for almost all pictures in Wasteland. The title.pic is a
 * pure PIC. Why it's not compressed like the base frame in the end.cpa or all
 * the pictures in the allpics and allhtds files is unknown. Maybe the
 * programmer forgot to compress the picture. But because the title.pic is the
 * simplest form of all the pictures it's the base for this class. So with this
 * class you can read and write this title pic right away.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Pic extends EgaImage implements Cloneable
{
    /**
     * Constructor
     * 
     * @param width
     *            The picture width
     * @param height
     *            The picture height
     */

    public Pic(int width, int height)
    {
        super(width, height);
    }


    /**
     * Creates a picture from a normal buffered image which is copied to the
     * picture. Colors are automatically converted to the 16 color palette of
     * the picture.
     * 
     * @param image
     *            The normal buffered image
     */

    public Pic(BufferedImage image)
    {
        super(image);
    }


    /**
     * Loads a xor-encoded picture from a stream.
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of the picture to read in pixel
     * @param height
     *            The height of the picture to read in pixel
     * @return The picture
     * @throws IOException
     */

    public static Pic read(InputStream stream, int width, int height)
        throws IOException
    {
        return read(stream, width, height, true);
    }
    

    /**
     * Loads a picture from a stream
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of the picture to read in pixel
     * @param height
     *            The height of the picture to read in pixel
     * @param encoded
     *            If the pic is vertical xor encoded
     * @return The picture
     * @throws IOException
     */

    public static Pic read(InputStream stream, int width, int height,
        boolean encoded) throws IOException
    {
        InputStream xorStream;
        Pic pic;
        int b;
        int x, y;

        pic = new Pic(width, height);
        if (encoded)
        {
            xorStream = new VerticalXorInputStream(stream, width);
        }
        else
        {
            xorStream = stream;
        }
        for (y = 0; y < height; y++)
        {
            for (x = 0; x < width; x += 2)
            {
                b = xorStream.read();
                if (b == -1)
                {
                    throw new EOFException(
                        "Unexpected end of stream while reading picture");
                }
                pic.setPixel(x, y, b >> 4);
                pic.setPixel(x + 1, y, b & 0xf);
            }
        }
        return pic;
    }


    /**
     * Writes the picture to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param xorEncode
     *            If the picture data should be vertical-xor encoded
     * @throws IOException
     */

    public void write(OutputStream stream, boolean xorEncode)
        throws IOException
    {
        int width, height;
        int x, y;
        OutputStream xorStream;

        width = getWidth();
        height = getHeight();
        if (xorEncode)
        {
            xorStream = new VerticalXorOutputStream(stream, width);
        }
        else
        {
            xorStream = stream;
        }
        for (y = 0; y < height; y++)
        {
            for (x = 0; x < width; x += 2)
            {
                xorStream.write((getPixel(x, y) << 4) | getPixel(x + 1, y));
            }
        }
    }


    /**
     * Writes the picture to the specified output stream. The picture data is
     * vertical-xor encoded.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        write(stream, true);
    }


    /**
     * Returns the bytes of the picture.
     * 
     * @param xorEncode
     *            If picture data should be vertical-xor encoded
     * @return The picture bytes
     */

    public byte[] getBytes(boolean xorEncode)
    {
        ByteArrayOutputStream stream;

        stream = new ByteArrayOutputStream();
        try
        {
            try
            {
                write(stream, xorEncode);
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
     * Returns the bytes of the picture. The picture data is vertical-xor
     * encoded.
     * 
     * @return The picture bytes
     */

    public byte[] getBytes()
    {
        return getBytes(true);
    }


    /**
     * Returns a copy of the picture.
     * 
     * @return The copy of the source image
     */

    @Override
    public Pic clone()
    {
        Pic dest;

        dest = new Pic(getWidth(), getHeight());
        dest.createGraphics().drawImage(this, 0, 0, null);
        return dest;
    }


    /**
     * Reads an image from the specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The image
     * @throws IOException
     */

    public static Pic read(InputStream stream) throws IOException
    {
        BufferedImage image;

        image = ImageIO.read(stream);
        if (image == null)
        {
            throw new IOException("Unable to read image from stream");
        }
        return new Pic(image);
    }
}
