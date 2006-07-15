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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.ailis.wlandsuite.utils.Colors;
import de.ailis.wlandsuite.utils.PicUtils;


/**
 * This writer writes Wasteland Pics to a stream or file from a standard
 * BufferedImage.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicWriter
{
    /** The singleton instance of the pic writer */
    private static PicWriter instance;


    /**
     * Returns the singleton instance of the pic writer.
     * 
     * @return The singleton instance of the pic writer
     */

    public static PicWriter getInstance()
    {
        if (instance == null)
        {
            instance = new PicWriter();
        }
        return instance;
    }


    /**
     * Writes a pic to a file.
     * 
     * @param file
     *            The file to write the pic to
     * @param image
     *            The image to write
     * @throws IOException
     */

    public void writePic(File file, BufferedImage image) throws IOException
    {
        OutputStream stream;

        stream = new FileOutputStream(file);
        try
        {
            writePic(stream, image);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Creates pic data from a buffered image.
     * 
     * @param image
     *            The buffered image
     * @return The pic data
     */

    public byte[] createPicData(BufferedImage image)
    {
        byte[] buffer;
        int width, height;
        int size;
        BufferedImage tmp;
        int[] palette;
        IndexColorModel colorModel;
        Graphics2D graphics;

        width = image.getWidth();
        height = image.getHeight();

        palette = Colors.CGA;
        colorModel = new IndexColorModel(4, palette.length, palette, 0, false,
            -1, DataBuffer.TYPE_BYTE);
        tmp = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED,
            colorModel);
        graphics = tmp.createGraphics();
        graphics.drawImage(image, null, 0, 0);

        size = width * height / 2;
        buffer = new byte[size];

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x += 2)
            {
                buffer[y * width / 2 + x / 2] = (byte) 
                    ((Colors.getCGAIndex(tmp
                    .getRGB(x, y)) << 4) | (Colors.getCGAIndex(tmp.getRGB(
                    x + 1, y))));
            }
        }
        return buffer;
    }


    /**
     * Writes a pic to an output stream.
     * 
     * @param stream
     *            The stream to write the pic to
     * @param image
     *            The image to write
     * @throws IOException
     */

    public void writePic(OutputStream stream, BufferedImage image)
        throws IOException
    {
        byte[] buffer;

        buffer = createPicData(image);
        PicUtils.encodeVXor(buffer, image.getWidth(), image.getHeight());
        stream.write(buffer);
    }
}
