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

package de.ailis.wlandsuite.game.blocks;

import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameBlockType;


/**
 * The interface for game blocks.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public interface GameBlock
{
    /**
     * Writes the game block to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException 
     */

    public void write(OutputStream stream) throws IOException;


    /**
     * Writes the game block to the specified output stream in XML format.
     * 
     * @param stream
     *            The output stream
     * @throws IOException 
     */

    public void writeXml(OutputStream stream) throws IOException;
    
    
    /**
     * Converts the game block into an XML element and returns it.
     * 
     * @return The XML element
     */
    
    public Element toXml();
    
    
    /**
     * Returns the game block type.
     * 
     * @return The game block type
     */
    
    public GameBlockType getType();
}
