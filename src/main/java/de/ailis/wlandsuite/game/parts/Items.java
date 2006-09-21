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

import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * The items of a character.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Items extends ArrayList<Item>
{
    /** Serial version UID */
     private static final long serialVersionUID = 3266288486991374891L;


    /**
     * Constructor
     */

    public Items()
    {
        super();
    }


    /**
     * Constructor
     * 
     * @param capacity
     *            The initial capacity
     */

    public Items(int capacity)
    {
        super(capacity);
    }


    /**
     * Creates and returns a new Items object by reading all the item from the
     * specified stream. The stream must be positioned at the beginning of the
     * item list.
     * 
     * @param stream
     *            The stream to read the items from
     * @return The items
     * @throws IOException
     */

    public static Items read(SeekableInputStream stream) throws IOException
    {
        Items items;
 
        items = new Items(30);

        for (int i = 0; i < 30; i++)
        {
            Item item = Item.read(stream);
            if (item.getId() != 0)
            {
                items.add(item);
            }
        }

        // Return the items
        return items;
    }


    /**
     * Creates and returns a new Items object from XML.
     * 
     * @param element
     *            The XML element
     * @return The items
     */

    public static Items read(Element element)
    {
        Items items;

        items = new Items(30);
        for (Object item: element.elements("item"))
        {
            Element subElement = (Element) item;
            
            items.add(Item.read(subElement));
        }
        return items;
    }


    /**
     * Writes the items to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException 
     */

    public void write(SeekableOutputStream stream) throws IOException
    {
        if (size() > 30)
        {
            throw new GameException("Character has to many items: " + size());
        }
        
        // Write the normal items
        for (Item item: this)
        {
            item.write(stream);
        }
        
        // Write the unused items
        for (int i = size(); i < 30; i++)
        {
            stream.write(0);
            stream.write(0);
        }
    }


    /**
     * Returns the monsters as XML.
     * 
     * @return The monsters as XML
     */

    public Element toXml()
    {
        Element element;

        // Create the root XML element
        element = XmlUtils.createElement("items");

        // Add all the items
        for (Item item: this)
        {
            element.add(item.toXml());
        }

        // Return the XML element
        return element;
    }
}
