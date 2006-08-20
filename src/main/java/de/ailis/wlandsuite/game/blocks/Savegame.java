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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameBlockType;
import de.ailis.wlandsuite.game.RotatingXorOutputStream;
import de.ailis.wlandsuite.game.parts.Part;


/**
 * Savegame block
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Savegame extends AbstractGameBlock
{
    /**
     * Constructor
     */
    
    private Savegame()
    {
        super(GameBlockType.SAVEGAME);        
    }
    
    
    /**
     * Builds a savegame block from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public Savegame(Element element)
    {
        this();
        processChildren(element);
    }

    
    /**
     * Constructor
     * 
     * @param bytes
     *            The block data
     */

    public Savegame(byte[] bytes)
    {
        this();
        parse(bytes);
    }


    /**
     * Parses the game block
     * 
     * @param bytes
     *            The bytes of the game block to parse
     */

    private void parse(byte[] bytes)
    {
        createUnknownParts(bytes);
    }

    
    /**
     * Returns the size of the encrypted part in the map block
     *
     * @return The size of the encrypted part
     */

    public static int getEncSize()
    {
        return 0x800;
    }
    

    /**
     * Writes the game block to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        OutputStream gameStream;
        int encSize;
        byte[] bytes;
        
        // Create the game block data
        bytes = createBlockData();

        // Only the first 0x800 bytes of the savegame block is encrypted
        encSize = getEncSize();

        // Write the encrypted data
        gameStream = new RotatingXorOutputStream(stream);
        gameStream.write(bytes, 0, encSize);
        gameStream.flush();

        // Write the unencrypted data
        stream.write(bytes, encSize, bytes.length - encSize);
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.AbstractGameBlock#toXml()
     */
    
    public Element toXml()
    {
        Element element;
        
        element = DocumentHelper.createElement("savegame");
        for (Part part: this.parts)
        {
            element.add(part.toXml());
        }
        return element;
    }
}
