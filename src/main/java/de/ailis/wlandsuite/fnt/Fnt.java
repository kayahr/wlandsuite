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

package de.ailis.wlandsuite.fnt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A Fnt object contains the font characters of Wastelands fnt file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Fnt implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = 3848735175735435359L;
    
    /** The cursors */
    private List<FntChar> chars;


    /**
     * Private constructor
     */

    private Fnt()
    {
        this.chars = new ArrayList<FntChar>();
    }


    /**
     * Constructor for building a new fnt object.
     * 
     * @param chars
     *            The chars
     */

    public Fnt(List<FntChar> chars)
    {
        this.chars = chars;
    }
    

    /**
     * Calculates and returns the number of font characters for the given file 
     * size.
     * 
     * @param size
     *            The file size
     * @return The number of characters
     */

    public static int getQuantity(long size)
    {
        return (int) (size / (4 * 8));
    }


    /**
     * Loads font characters from a stream. A quantity of 172 characters is
     * assumed.
     * 
     * @param stream
     *            The input stream
     * @return The fnt
     * @throws IOException
     */

    public static Fnt read(InputStream stream) throws IOException
    {
        return read(stream, 172);
    }


    /**
     * Loads font characters from a stream.
     * 
     * @param stream
     *            The input stream
     * @param quantity
     *            The number of characters
     * @return The fnt
     * @throws IOException
     */

    public static Fnt read(InputStream stream, int quantity) throws IOException
    {
        Fnt fnt;

        // Create the Fnt
        fnt = new Fnt();

        // Read the font characters
        for (int i = 0; i < quantity; i++)
        {
            fnt.chars.add(FntChar.read(stream));
        }

        // Return the fnt
        return fnt;
    }


    /**
     * Writes font characters to a stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        // Read the cursors
        for (FntChar fntChar: this.chars)
        {
            fntChar.write(stream);
        }
    }


    /**
     * Returns the font characters
     *
     * @return The font characters
     */
    
    public List<FntChar> getChars()
    {
        return this.chars;
    }
}
