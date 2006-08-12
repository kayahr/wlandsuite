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


/**
 * Abstract part
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class AbstractPart implements Part, Comparable<Part>
{
    /** The offset of the part in the game block */
    protected int offset;

    /** The size of the part */
    protected int size;

    
    /**
     * Constructor
     */
    
    protected AbstractPart()
    {
        super();
    }
    

    /**
     * Constructor
     * 
     * @param offset
     *            The offset
     * @param size
     *            The size
     */

    protected AbstractPart(int offset, int size)
    {
        this.offset = offset;
        this.size = size;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#getOffset()
     */

    public int getOffset()
    {
        return this.offset;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#getSize()
     */

    public int getSize()
    {
        return this.size;
    }


    /**
     * @see java.lang.Comparable#compareTo(Object)
     * 
     * @param o
     *            The object to compare with
     * @return The compare result
     */

    public int compareTo(Part o)
    {
        if (this.offset < o.getOffset())
        {
            return -1;
        }
        else if (this.offset > o.getOffset())
        {
            return 1;
        }
        return 0;
    }
}
