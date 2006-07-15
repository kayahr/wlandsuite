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
import java.util.List;

import de.ailis.wlandsuite.utils.Colors;


/**
 * This writer writes Wasteland WLF masks to a stream or file from a list of
 * standard BufferedImage objects.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class WlfWriter
{
    /** The singleton instance of the wlf writer */
    private static WlfWriter instance;


    /**
     * Returns the singleton instance of the wlf writer.
     * 
     * @return The singleton instance of the wlf writer
     */

    public static WlfWriter getInstance()
    {
        if (instance == null)
        {
            instance = new WlfWriter();
        }
        return instance;
    }


    /**
     * Writes a list of wlf masks to a file.
     * 
     * @param file
     *            The file to write the wlf to
     * @param masks
     *            The list of wlf masks to write
     * @throws IOException
     */

    public void writeWlf(File file, List<BufferedImage> masks)
        throws IOException
    {
        OutputStream stream;

        stream = new FileOutputStream(file);
        try
        {
            writeWlf(stream, masks);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Creates wlf data from a list of buffered images
     * 
     * @param masks
     *            The masks
     * @return The wlf data
     */

    public byte[] createWlfData(List<BufferedImage> masks)
    {
        byte[] buffer;
        int width, height;
        int maskSize, size;
        BufferedImage tmp;
        int[] palette;
        IndexColorModel colorModel;
        Graphics2D graphics;
        byte b, bit;
        int maskNo;

        width = 0;
        height = 0;
        for (BufferedImage mask: masks)
        {
            if (mask.getWidth() > width)
            {
                width = mask.getWidth();
            }
            if (mask.getHeight() > height)
            {
                height = mask.getHeight();
            }
        }

        maskSize = width * height / 8;
        size = maskSize * masks.size();
        buffer = new byte[size];

        palette = Colors.BW;
        colorModel = new IndexColorModel(1, palette.length, palette, 0, false,
            -1, DataBuffer.TYPE_BYTE);
        tmp = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED,
            colorModel);

        maskNo = 0;
        for (BufferedImage mask: masks)
        {
            graphics = tmp.createGraphics();
            graphics.drawImage(mask, 0, 0, null);

            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x += 8)
                {
                    b = 0;
                    for (bit = 0; bit < 8; bit++)
                    {
                        b |= Colors.getBWIndex(tmp.getRGB(x + bit, y)) << (7 - bit);
                    }
                    buffer[y * width / 8 + x / 8 + maskNo * maskSize] = b;
                }
            }
            maskNo++;
        }
        return buffer;
    }


    /**
     * Writes wlf masks to an output stream.
     * 
     * @param stream
     *            The stream to write the wlf to
     * @param masks
     *            The wlf masks to write
     * @throws IOException
     */

    public void writeWlf(OutputStream stream, List<BufferedImage> masks)
        throws IOException
    {
        stream.write(createWlfData(masks));
    }
}
