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


/**
 * Converters
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicUtils
{
    /**
     * Calculates the pic height from the pic width and filesize and bit depth.
     * 
     * @param width
     *            The pic width
     * @param size
     *            The data size
     * @param bitdepth
     *            The bitdepth
     * @return The calculated pic height
     */

    public static int getHeight(int width, long size, int bitdepth)
    {
        if ((size * 8 / bitdepth) % width != 0)
        {
            throw new IllegalArgumentException("Invalid width specified");
        }
        return (int) (size * 8 / bitdepth) / width;
    }


    /**
     * VXor-decodes a buffer.
     * 
     * @param buffer
     *            The buffer to decode
     * @param width
     *            The width in pixel
     * @param height
     *            The height in pixel
     */

    public static void decodeVXor(byte[] buffer, int width, int height)
    {
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width / 2; x++)
            {
                if (y > 0)
                {
                    buffer[y * width / 2 + x] ^= buffer[(y - 1) * width / 2 + x];
                }
            }
        }
    }


    /**
     * VXor-encodes a buffer.
     * 
     * @param buffer
     *            The buffer to encode
     * @param width
     *            The width in pixel
     * @param height
     *            The height in pixel
     */

    public static void encodeVXor(byte[] buffer, int width, int height)
    {
        byte[] line;
        byte b;

        line = new byte[width / 2];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width / 2; x++)
            {
                b = buffer[y * width / 2 + x];
                if (y > 0)
                {
                    buffer[y * width / 2 + x] ^= line[x];
                }
                line[x] = b;
            }
        }
    }
}
