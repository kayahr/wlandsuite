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
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.utils.Colors;
import de.ailis.wlandsuite.utils.WlfUtils;


/**
 * This reader reads Wasteland masks from a stream or file into a list of
 * BufferedImage objects.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class WlfReader
{
    /** The singleton instance of the wlf reader */
    private static WlfReader instance;


    /**
     * Returns the singleton instance of the wlf reader.
     * 
     * @return The singleton instance of the wlf reader
     */

    public static WlfReader getInstance()
    {
        if (instance == null)
        {
            instance = new WlfReader();
        }
        return instance;
    }


    /**
     * Reads wfl masks from a file. The width and height of the masks must be
     * specified because the size can't be read from the WLF file.
     * 
     * @param file
     *            The file to read the wlf masks from
     * @param width
     *            The mask width
     * @param height
     *            The mask height
     * @return The image
     * @throws IOException
     */

    public List<BufferedImage> readWlf(File file, int width, int height)
        throws IOException
    {
        int quantity;
        InputStream stream;

        quantity = WlfUtils.getNumberOfMasks(width, height, file.length(), 1);
        stream = new FileInputStream(file);
        try
        {
            return readWlf(stream, width, height, quantity);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Reads wlf masks from an input stream. The width and height and the number
     * of masks must be specified because these information can't be read from a
     * wlf stream.
     * 
     * @param stream
     *            The stream to read the wlf from
     * @param width
     *            The image width
     * @param height
     *            The image height
     * @param quantity
     *            The number of masks to read
     * @return The image
     * @throws IOException
     */

    public List<BufferedImage> readWlf(InputStream stream, int width,
        int height, int quantity) throws IOException
    {
        byte[] buffer;
        int size;
        int read;
        int current;

        size = quantity * width * height / 8;
        buffer = new byte[size];
        read = 0;
        while (read < size)
        {
            current = stream.read(buffer, read, size - read);
            if (current == -1)
            {
                throw new IOException(
                    "Unexpected end of WLF stream. Tried to read " + size
                        + " bytes but only got " + read + " bytes.");
            }
            read += current;
        }
        return readWlf(buffer, width, height);
    }


    /**
     * Converts WLF data into a list of BufferedImage objects. The width and the
     * height must be specified because mask dimensions can't be read from a
     * wlf.
     * 
     * @param buffer
     *            The wlf data
     * @param width
     *            The image width
     * @param height
     *            The image height
     * @return The image
     */

    public List<BufferedImage> readWlf(byte[] buffer, int width, int height)
    {
        List<BufferedImage> masks;
        BufferedImage mask;
        int quantity;
        int i, x, y;
        int size;
        int[] palette;
        int b, bit;
        IndexColorModel colorModel;

        size = width * height / 8;
        quantity = WlfUtils.getNumberOfMasks(width, height, buffer.length, 1);
        masks = new ArrayList<BufferedImage>(quantity);
        palette = Colors.BW;
        colorModel = new IndexColorModel(1, palette.length, palette, 0, false,
            -1, DataBuffer.TYPE_BYTE);
        for (i = 0; i < quantity; i++)
        {
            mask = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_BINARY, colorModel);
            for (y = 0; y < height; y++)
            {
                for (x = 0; x < width / 8; x++)
                {
                    b = 0xff & buffer[y * width / 8 + x + i * size];
                    for (bit = 0; bit < 8; bit++)
                    {
                        mask.setRGB(x * 8 + bit, y,
                            palette[(b >> (7 - bit)) & 1]);
                    }
                }
            }
            masks.add(mask);
        }
        return masks;
    }
}
