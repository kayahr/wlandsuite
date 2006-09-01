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

package de.ailis.wlandsuite.game.chartable;


/**
 * CharTableEntry
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CharTableEntry implements Comparable<CharTableEntry>
{
    /** The character */
    private int character;

    /** How many times the character of this entry is used */
    private int counter;


    /**
     * Constructor
     * 
     * @param character
     *            The character
     * @param counter
     *            How many times the character is already used
     */

    public CharTableEntry(int character, int counter)
    {
        this.character = character;
        this.counter = counter;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * 
     * @param other
     *            The other object to compare this one with
     * @return The compare result
     */

    public int compareTo(CharTableEntry other)
    {
        if (this.counter > other.counter)
        {
            return -1;
        }
        else if (this.counter < other.counter)
        {
            return 1;
        }
        return 0;
    }
    
    
    /**
     * Increments the usage counter.
     */
    
    public void incrementCounter()
    {
        this.counter++;
    }

    
    /**
     * Returns the character.
     *
     * @return The character
     */
    
    public int getCharacter()
    {
        return this.character;
    }

    
    /**
     * Returns the counter.
     *
     * @return The counter
     */
    
    public int getCounter()
    {
        return this.counter;
    }
}
