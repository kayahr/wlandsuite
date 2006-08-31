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
 * Shop items block
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ShopItems extends AbstractGameBlock
{
    /**
     * Constructor
     */
    
    private ShopItems()
    {
        super(GameBlockType.SHOPITEMS);
    }
    
    
    /**
     * Builds a shopitems block from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public ShopItems(Element element)
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

    public ShopItems(byte[] bytes)
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
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#write(java.io.OutputStream, boolean)
     */
    
    public void write(OutputStream stream, boolean encrypt) throws IOException
    {
        OutputStream gameStream;
        byte[] bytes;
        
        // Create the game block data
        bytes = createBlockData();

        // Write the encrypted data
        if (encrypt)
        {
            gameStream = new RotatingXorOutputStream(stream);
        }
        else
        {
            gameStream = stream;

            // Write dummy checksum to keep the filesize
            gameStream.write(0);
            gameStream.write(0);
        }
        gameStream.write(bytes);
        gameStream.flush();
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.AbstractGameBlock#toXml()
     */
    
    public Element toXml()
    {
        Element element;
        
        element = DocumentHelper.createElement("shopitems");
        for (Part part: this.parts)
        {
            element.add(part.toXml());
        }
        return element;
    }
}
