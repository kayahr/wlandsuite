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
import java.io.InputStream;
import java.io.OutputStream;

import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;


/**
 * A item as used by the Char class.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Item
{
    /** The item id */
    private int id;

    /** The load */
    private int load;


    /**
     * Creates and returns a new item by reading its data from the specified
     * stream.
     * 
     * @param stream
     *            The input stream
     * @return The item
     * @throws IOException
     */

    public static Item read(InputStream stream) throws IOException
    {
        Item item;

        item = new Item();

        item.id = stream.read();
        item.load = stream.read();

        return item;
    }


    /**
     * Writes the item to the specified stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        stream.write(this.id);
        stream.write(this.load);
    }


    /**
     * Returns the item data as XML.
     * 
     * @return The item data as XML
     */

    public Element toXml()
    {
        Element element;

        element = XmlUtils.createElement("item");
        element.addAttribute("id", Integer.toString(this.id));
        if (this.load != 0)
        {
            element.addAttribute("load", Integer.toString(this.load));
        }

        return element;
    }


    /**
     * Creates and returns a new item object by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The item data
     */

    public static Item read(Element element)
    {
        Item item;

        item = new Item();

        item.id = StringUtils.toInt(element.attributeValue("id"));
        item.load = StringUtils.toInt(element.attributeValue("load", "0"));

        return item;
    }


    /**
     * Returns the item.
     * 
     * @return The item
     */

    public int getId()
    {
        return this.id;
    }


    /**
     * Sets the item.
     * 
     * @param item
     *            The item to set
     */

    public void setId(int item)
    {
        this.id = item;
    }


    /**
     * Returns the load.
     * 
     * @return The load
     */

    public int getLoad()
    {
        return this.load;
    }


    /**
     * Sets the load.
     * 
     * @param load
     *            The load to set
     */

    public void setLoad(int load)
    {
        this.load = load;
    }
}
