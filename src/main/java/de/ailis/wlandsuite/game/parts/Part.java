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
import java.io.OutputStream;

import org.dom4j.Element;


/**
 * Part
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public interface Part extends Comparable<Part>
{
    /**
     * Converts the game block part into a XML element.
     * 
     * @return The XML element
     */

    public Element toXml();


    /**
     * Writes the part to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException;


    /**
     * Returns the offset from which the part was read. This is only possible
     * (and needed) if the part was read from a game file or is an unknown
     * block (for which the offset is also stored in the XML file). Otherwise it
     * will return -1.
     * 
     * @return The offset of the part or -1 if not available
     */

    public int getOffset();


    /**
     * Returns the size of the read part. This is only possible (and needed) if
     * the part was read from a game file or is an unknown block (for which
     * the size is also stored in the XML file). Otherwise it will return -1.
     * 
     * @return The size of the part or -1 if not available
     */

    public int getSize();
}
