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

package de.ailis.wlandsuite.rawgame.parts;


/**
 * The base class for all the parts implementing functionality which is common
 * to all parts.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class AbstractPart implements Part
{
    /** The part size (only available if part was read from game file) */
    protected int size = -1;

    /** The part offset (only available if part was read from game file) */
    protected int offset = -1;


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#getOffset()
     */

    public int getOffset()
    {
        return this.offset;
    }

    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#getSize()
     */

    public int getSize()
    {
        return this.size;
    }


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * 
     * @param other
     *            The other part to compare with
     * @return The compare result
     */

    public int compareTo(Part other)
    {
        if (this.offset < other.getOffset())
        {
            return -1;
        }
        else if (this.offset > other.getOffset())
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    
    /**
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "@" + this.offset + "+" + this.size;
    }
}
