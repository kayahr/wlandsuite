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

package de.ailis.wlandsuite.curs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A Curs object contains the cursors of Wastelands curs file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Curs implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -6430987170817521049L;

    /** The cursors */
    private List<Cursor> cursors;


    /**
     * Private constructor
     */

    private Curs()
    {
        this.cursors = new ArrayList<Cursor>();
    }


    /**
     * Constructor for building a new curs.
     * 
     * @param cursors
     *            The cursors
     */

    public Curs(List<Cursor> cursors)
    {
        this.cursors = cursors;
    }


    /**
     * Calculates and returns the number of sprites for the given file size,
     * cursor width and height
     * 
     * @param size
     *            The file size
     * @param width
     *            The cursor width
     * @param height
     *            The cursor height
     * @return The number of cursors
     */

    public static int getQuantity(long size, int width, int height)
    {
        return (int) (size / (4 * height * width / 8 * 2));
    }


    /**
     * Loads cursors from a stream. A cursor size of 16x16 and a quantity of 8
     * is assumed.
     * 
     * @param stream
     *            The input stream
     * @return The cursors
     * @throws IOException
     */

    public static Curs read(InputStream stream) throws IOException
    {
        return read(stream, 16, 16, 8);
    }


    /**
     * Loads cursors from a stream. A cursor size of 16x16 is assumed.
     * 
     * @param stream
     *            The input stream
     * @param quantity
     *            The number of cursors to read
     * @return The cursors
     * @throws IOException
     */

    public static Curs read(InputStream stream, int quantity)
        throws IOException
    {
        return read(stream, 16, 16, quantity);
    }


    /**
     * Loads cursors from a stream.
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of the cursors
     * @param height
     *            The height of the cursors
     * @param quantity
     *            The number of cursors
     * @return The cursors
     * @throws IOException
     */

    public static Curs read(InputStream stream, int width, int height,
        int quantity) throws IOException
    {
        Curs curs;

        // Create the Curs
        curs = new Curs();

        // Read the cursors
        for (int i = 0; i < quantity; i++)
        {
            curs.cursors.add(Cursor.read(stream, width, height));
        }

        // Return the curs
        return curs;
    }


    /**
     * Writes cursors to a stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        // Read the cursors
        for (Cursor cursor: this.cursors)
        {
            cursor.write(stream);
        }
    }


    /**
     * Returns the cursors.
     * 
     * @return The cursors
     */

    public List<Cursor> getCursors()
    {
        return this.cursors;
    }
}
