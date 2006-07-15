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

package de.ailis.wlandsuite.io;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.ailis.wlandsuite.utils.Colors;
import de.ailis.wlandsuite.utils.PicUtils;


/**
 * This reader reads Wasteland Pics from a stream or file into a standard
 * BufferedImage.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicReader
{
    /** The singleton instance of the pic reader */
    private static PicReader instance;


    /**
     * Returns the singleton instance of the pic reader.
     * 
     * @return The singleton instance of the pic reader
     */

    public static PicReader getInstance()
    {
        if (instance == null)
        {
            instance = new PicReader();
        }
        return instance;
    }

    
    /**
     * Reads a pic from a file. The width must be specified because the image
     * dimensions can't be read from a pic. The height is calculated from the
     * width and the file size
     * 
     * @param file
     *            The file to read the pic from
     * @param width
     *            The image width
     * @return The image
     * @throws IOException
     */

    public BufferedImage readPic(File file, int width) throws IOException
    {
        int height;
        InputStream stream;

        height = PicUtils.getHeight(width, file.length(), 4);
        stream = new FileInputStream(file);
        try
        {
            return readPic(stream, width, height);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Reads a pic from an input stream. The width and height must be specified
     * because the image dimensions can't be read from a pic.
     * 
     * @param stream
     *            The stream to read the pic from
     * @param width
     *            The image width
     * @param height
     *            The image height
     * @return The image
     * @throws IOException
     */

    public BufferedImage readPic(InputStream stream, int width, int height)
        throws IOException
    {
        byte[] buffer;
        int size;
        int read;
        int current;

        size = width * height / 2;
        buffer = new byte[size];
        read = 0;
        while (read < size)
        {
            current = stream.read(buffer, read, size - read);
            if (current == -1)
            {
                throw new IOException("Unexpected end of PIC stream. Tried to read " + size
                    + " bytes but only got " + read + " bytes.");
            }
            read += current;
        }
        return readPic(buffer, width);
    }


    /**
     * Converts PIC data into a buffered image. The width must be specified
     * because image dimensions can't be read from a pic. The height is
     * calculated automatically from the data size and the width.
     * 
     * @param buffer
     *            The pic data
     * @param width
     *            The image width
     * @return The image
     */

    public BufferedImage readPic(byte[] buffer, int width)
    {
        BufferedImage image;
        int height;
        int x, y;
        int[] palette;
        int b;

        height = PicUtils.getHeight(width, buffer.length, 4);
        palette = Colors.CGA;
        IndexColorModel colorModel = new IndexColorModel(4, palette.length,
            palette, 0, false, -1, DataBuffer.TYPE_BYTE);
        image = new BufferedImage(width, height,
            BufferedImage.TYPE_BYTE_BINARY, colorModel);
        PicUtils.decodeVXor(buffer, width, height);
        for (y = 0; y < height; y++)
        {
            for (x = 0; x < width / 2; x++)
            {
                b = 0xff & buffer[y * width / 2 + x];
                image.setRGB(x * 2, y, palette[b >> 4]);
                image.setRGB(x * 2 + 1, y, palette[b & 0x0f]);
            }
        }
        return image;
    }
}
