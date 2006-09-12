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

package de.ailis.wlandsuite.game;


/**
 * Describes the offset and the size of an MSQ block.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class GameMsqBlock
{
    /** The MSQ block offset */
    private int offset;

    /** The MSQ block size */
    private int size;

    
    /**
     * Constructor
     * 
     * @param offset
     *            The MSQ block offset
     * @param size
     *            The MSQ block size
     */

    public GameMsqBlock(int offset, int size)
    {
        this.offset = offset;
        this.size = size;
    }

    
    /**
     * Returns the MSQ block offset.
     *
     * @return The MSQ block offset
     */
    
    public int getOffset()
    {
        return this.offset;
    }


    /**
     * Returns the MSQ block size.
     *
     * @return The MSQ block size
     */
    
    public int getSize()
    {
        return this.size;
    }
}
