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

package de.ailis.wlandsuite.game.parts;

import java.io.IOException;
import java.util.ArrayList;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * The special action table is used as a map from action indexes to actions.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class SpecialActionTable extends ArrayList<Integer>
{
    /** Serial version UID */
    private static final long serialVersionUID = 72194936032899891L;


    /**
     * Constructs a new action table
     */

    public SpecialActionTable()
    {
        super();
    }


    /**
     * Constructor
     *
     * @param capacity
     *            The initial capacity
     */

    public SpecialActionTable(final int capacity)
    {
        super(capacity);
    }


    /**
     * Creates and returns a new Action Table object by reading the data from
     * the specified stream.
     *
     * @param stream
     *            The stream to read the data from
     * @param quantity
     *            The number of action table entries to read from the stream
     * @return The action table
     * @throws IOException
     *             When file operation fails.
     */

    public static SpecialActionTable read(final SeekableInputStream stream, final int quantity)
        throws IOException
    {
        SpecialActionTable table;

        table = new SpecialActionTable(quantity);

        for (int i = 0; i < quantity; i++)
        {
            table.add(stream.readWord());
        }

        // Return the strings
        return table;
    }


    /**
     * Writes the action table to the specified stream.
     *
     * @param stream
     *            The stream to write the action table to
     */

    public void write(final SeekableOutputStream stream)
    {
        for (final int action: this)
        {
            stream.writeWord(action);
        }
    }
}
