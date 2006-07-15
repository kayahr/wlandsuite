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
 * Colors
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Colors
{
    /** CGA color palette */
    public static final int CGA[] = { 0xff000000, 0xff0000aa, 0xff00aa00,
        0xff00aaaa, 0xffaa0000, 0xffaa00aa, 0xffaa5500, 0xffaaaaaa, 0xff555555,
        0xff5555ff, 0xff55ff55, 0xff55ffff, 0xffff5555, 0xffff55ff, 0xffffff55,
        0xffffffff };

    /** Black & White color palette */
    public static final int BW[] = { 0xff000000, 0xffffffff };


    /**
     * Private constructor because of static class
     */

    private Colors()
    {
        super();
    }


    /**
     * Returns the CGA palette index for the specified color. Returns -1 if the
     * color was not found in the CGA palette.
     * 
     * @param color
     *            The color
     * @return The palette index
     */

    public static int getCGAIndex(int color)
    {
        for (int i = 0; i < CGA.length; i++)
        {
            if (CGA[i] == color) return i;
        }
        return -1;
    }


    /**
     * Returns the BW palette index for the specified color. Returns -1 if the
     * color was not found in the BW palette.
     * 
     * @param color
     *            The color
     * @return The palette index
     */

    public static int getBWIndex(int color)
    {
        for (int i = 0; i < BW.length; i++)
        {
            if (BW[i] == color) return i;
        }
        return -1;
    }
}
