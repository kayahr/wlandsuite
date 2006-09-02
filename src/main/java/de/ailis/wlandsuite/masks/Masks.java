/*
 * $Id:Wlf.java 81 2006-09-02 12:10:44Z k $
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

package de.ailis.wlandsuite.masks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * A WLF is a list of bit masks. Wasteland has two files in this format: The
 * masks.wlf and the ic0_9.wlf. This class represents such a list of bit masks
 * and can read and write the Wasteland files.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision:81 $
 */

public class Masks
{
    /** The list of WLF masks */
    private List<Mask> masks;


    /**
     * Constructor
     * 
     * @param masks
     *            The WLF masks
     */

    public Masks(List<Mask> masks)
    {
        this.masks = masks;
    }


    /**
     * Private constructor
     */

    private Masks()
    {
        this.masks = new ArrayList<Mask>();
    }


    /**
     * Reads wlf masks from an input stream. A standard width of 16x16 is
     * assumed.
     * 
     * @param stream
     *            The stream to read the wlf from
     * @param quantity
     *            The number of masks to read
     * @return The image
     * @throws IOException
     */

    public static Masks read(InputStream stream, int quantity) throws IOException
    {
        return read(stream, 16, 16, quantity);
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

    public static Masks read(InputStream stream, int width, int height,
        int quantity) throws IOException
    {
        Masks wlf;
        Mask mask;

        wlf = new Masks();
        for (int i = 0; i < quantity; i++)
        {
            mask = Mask.read(stream, width, height);
            wlf.masks.add(mask);
        }
        return wlf;
    }


    /**
     * Writes a WLF to the given output stream
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        for (Mask mask: this.masks)
        {
            mask.write(stream);
        }
    }


    /**
     * Calculates the number of wlf masks.
     * 
     * @param width
     *            The mask width
     * @param height
     *            The mask height
     * @param size
     *            The data size
     * @return The calculated number of wlf masks
     */

    public static int getNumberOfMasks(int width, int height, long size)
    {
        if ((size * 8) % (width * height) != 0)
        {
            throw new IllegalArgumentException("Invalid mask size specified");
        }
        return (int) (size * 8) / width / height;
    }


    /**
     * Returns the masks.
     *
     * @return The masks
     */
    
    public List<Mask> getMasks()
    {
        return this.masks;
    }
}
