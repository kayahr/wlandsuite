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

package de.ailis.wlandsuite.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;


/**
 * Wlf utility methods
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class WlfUtils
{
    /**
     * Calculates the number of wlf masks.
     * 
     * @param width
     *            The mask width
     * @param height
     *            The mask height
     * @param size
     *            The data size
     * @param bitdepth
     *            The bitdepth
     * @return The calculated number of wlf masks
     */

    public static int getNumberOfMasks(int width, int height, long size,
        int bitdepth)
    {
        if ((size * 8 / bitdepth) % (width * height) != 0)
        {
            throw new IllegalArgumentException("Invalid mask size specified");
        }
        return (int) (size * 8 / bitdepth) / width / height;
    }


    /**
     * Joins a list of BufferedImage objects representing the WLF masks into a
     * single BufferedImage.
     * 
     * @param masks
     *            The masks
     * @return The joined masks
     */

    public static BufferedImage join(List<BufferedImage> masks)
    {
        BufferedImage joined;
        int width;
        int height;
        int[] palette;
        IndexColorModel colorModel;
        int y;
        Graphics2D graphics;

        height = 0;
        width = 0;
        for (BufferedImage mask: masks)
        {
            if (mask.getWidth() > width)
            {
                width = mask.getWidth();
            }
            height += mask.getHeight();
        }

        palette = Colors.BW;
        colorModel = new IndexColorModel(1, palette.length, palette, 0, false,
            -1, DataBuffer.TYPE_BYTE);
        joined = new BufferedImage(width, height,
            BufferedImage.TYPE_BYTE_BINARY, colorModel);
        graphics = joined.createGraphics();
        y = 0;
        for (BufferedImage mask: masks)
        {
            graphics.drawImage(mask, 0, y, null);
            y += mask.getHeight();
        }
        return joined;
    }


    /**
     * Splits the specified image into a list of mask images. The quantity can
     * be specified. If it's 0 then the number of masks is automatically
     * calculated by using the image width as the mask height.
     * 
     * @param image
     *            The image containing all the masks (one mask per row)
     * @param quantity
     *            The number of masks in the image
     * @return The list of mask images
     */

    public static List<BufferedImage> split(BufferedImage image, int quantity)
    {
        List<BufferedImage> masks;
        BufferedImage mask;
        int width, height;
        int[] palette;
        IndexColorModel colorModel;
        Graphics2D graphics;
        
        if (quantity == 0)
        {
            quantity = image.getHeight() / image.getWidth();
        }
        width = image.getWidth();
        height = image.getHeight() / quantity;

        masks = new ArrayList<BufferedImage>(quantity);
        for (int i = 0; i < quantity; i++)
        {
            palette = Colors.BW;
            colorModel = new IndexColorModel(4, palette.length, palette, 0, false,
                -1, DataBuffer.TYPE_BYTE);
            mask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED,
                colorModel);
            graphics = mask.createGraphics();
            graphics.drawImage(image, 0, 0, width, height, 0, i * height, width, width + i * height, null);
            masks.add(mask);
        }
        return masks;
    }
}
