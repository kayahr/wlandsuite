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

package de.ailis.wlandsuite.rawgame.chartable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * CharTable
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CharTable
{
    /** The table entries */
    private List<CharTableEntry> entries;

    /** If this char table is finished and therefor read only */
    private boolean finished;


    /**
     * Constructor
     */

    public CharTable()
    {
        this.entries = new ArrayList<CharTableEntry>(60);
        this.finished = false;
    }


    /**
     * Constructor for reading a char table from an input stream.
     * 
     * @param stream
     *            The input stream
     * @throws IOException
     */

    public CharTable(InputStream stream) throws IOException
    {
        this.entries = new ArrayList<CharTableEntry>(60);
        for (int i = 60; i > 0; i--)
        {
            int b = stream.read();
            this.entries.add(new CharTableEntry(b, i));
        }
        Collections.sort(this.entries);
        this.finished = true;
    }


    /**
     * Adds a character to the char table.
     * 
     * @param character
     *            The character to add
     */

    public void add(int character)
    {
        if (this.finished)
        {
            throw new IllegalStateException("CharTable is already finished");
        }
        for (CharTableEntry entry: this.entries)
        {
            if (entry.getCharacter() == character)
            {
                entry.incrementCounter();
                return;
            }
        }
        this.entries.add(new CharTableEntry(character, 1));
    }


    /**
     * Adds the characters of the specified string to the char table.
     * 
     * @param string
     *            The string to add
     */

    public void add(String string)
    {
        if (this.finished)
        {
            throw new IllegalStateException("CharTable is already finished");
        }
        try
        {
            for (byte b: string.toLowerCase().getBytes("ASCII"))
            {
                int i = b & 0xff;

                add(i);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            // Ignored, can't happen
        }
    }


    /**
     * Finishes the char table
     */

    public void finish()
    {
        for (int i = 60 - this.entries.size(); i > 0; i--)
        {
            this.entries.add(new CharTableEntry(0x7f, 0));
        }
        if (this.entries.size() != 60)
        {
            throw new IllegalStateException(
                "CharTable has not 60 characters! It has "
                    + this.entries.size());
        }
        Collections.sort(this.entries);
        this.finished = true;
    }


    /**
     * Returns the character for the specified table index
     * 
     * @param index
     *            The table index
     * @return The character
     */

    public int getCharacter(int index)
    {
        return this.entries.get(index).getCharacter();
    }


    /**
     * Returns the index for the specified character. Return -1 if not found.
     * 
     * @param character
     *            The character to search for
     * @return The index in the char table
     */

    public int getIndex(int character)
    {
        for (int i = 0, max = this.entries.size(); i < max; i++)
        {
            if (this.entries.get(i).getCharacter() == character)
            {
                return i;
            }
        }
        return -1;
    }


    /**
     * Writes the char table to the specified output stream.
     * 
     * @param stream
     *            The output stream to write to
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        if (!this.finished) finish();

        for (CharTableEntry entry: this.entries)
        {
            stream.write(entry.getCharacter());
        }
    }


    /**
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString()
    {
        StringBuilder builder;

        if (!this.finished) finish();

        builder = new StringBuilder();
        for (CharTableEntry entry: this.entries)
        {
            int i = entry.getCharacter();

            if (i >= 0x20 && i < 0x7f)
            {
                builder.append(new String(new byte[] { (byte) i }));
            }
            else
            {
                builder.append(String.format("\\%02x", new Object[] { i }));
            }
        }
        return builder.toString();
    }
}
