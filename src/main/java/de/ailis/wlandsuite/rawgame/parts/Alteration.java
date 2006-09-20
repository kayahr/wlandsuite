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

import java.io.IOException;

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitOutputStream;


/**
 * Alteration
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Alteration
{
    /** The flags */
    private int unknown;

    /** If coordinates are relative */
    private boolean relative;

    /** The x coordinate */
    private int x;

    /** The y coordinate */
    private int y;

    /** The new action class to set */
    private int actionClass;

    /** The new action selector to set */
    private int actionSelector;


    /**
     * Constructor
     * 
     * @param stream
     *            The stream to read the alteration data from
     * @param flags
     *            The flags
     * @throws IOException
     */

    public Alteration(BitInputStream stream, int flags) throws IOException
    {
        this.unknown = flags & 0x7e;
        this.relative = (flags & 1) == 1;
        this.x = stream.readSignedByte();
        this.y = stream.readSignedByte();
        this.actionClass = stream.readByte();
        this.actionSelector = stream.readByte();
    }


    /**
     * Creates the central directory from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public Alteration(Element element)
    {
        super();

        this.unknown = Integer.parseInt(element.attributeValue("unknown"));
        this.relative = Boolean
            .parseBoolean(element.attributeValue("relative"));
        this.x = Integer.parseInt(element.attributeValue("x"));
        this.y = Integer.parseInt(element.attributeValue("y"));
        this.actionClass = Integer.parseInt(element.attributeValue("class",
            "255"));
        this.actionSelector = Integer.parseInt(element.attributeValue(
            "selector", "255"));
    }


    /**
     * Converts the alteration data to XML and returns the XML element.
     * 
     * @return The XML element
     */

    public Element toXml()
    {
        Element element;

        element = XMLUtils.createElement("alteration");
        element.addAttribute("unknown", Integer.toString(this.unknown));
        element.addAttribute("relative", this.relative ? "true" : "false");
        element.addAttribute("x", Integer.toString(this.x));
        element.addAttribute("y", Integer.toString(this.y));
        element.addAttribute("class", Integer.toString(this.actionClass));
        element.addAttribute("selector", Integer.toString(this.actionSelector));
        return element;
    }


    /**
     * Writes the alteration data to a stream.
     * 
     * @param stream
     *            The stream to write to
     * @param last
     *            If this is the last alteration block
     * @throws IOException
     */

    public void write(BitOutputStream stream, boolean last) throws IOException
    {
        stream.writeByte(this.unknown | (last ? 128 : 0)
            | (this.relative ? 1 : 0));
        stream.writeSignedByte(this.x);
        stream.writeSignedByte(this.y);
        stream.writeByte(this.actionClass);
        stream.writeByte(this.actionSelector);
    }


    /**
     * Returns the actionClass.
     * 
     * @return The actionClass
     */

    public int getActionClass()
    {
        return this.actionClass;
    }


    /**
     * Sets the actionClass.
     * 
     * @param actionClass
     *            The actionClass to set
     */

    public void setActionClass(int actionClass)
    {
        this.actionClass = actionClass;
    }


    /**
     * Returns the actionSelector.
     * 
     * @return The actionSelector
     */

    public int getActionSelector()
    {
        return this.actionSelector;
    }


    /**
     * Sets the actionSelector.
     * 
     * @param actionSelector
     *            The actionSelector to set
     */

    public void setActionSelector(int actionSelector)
    {
        this.actionSelector = actionSelector;
    }


    /**
     * Returns the flags.
     * 
     * @return The flags
     */

    public int getUnknown()
    {
        return this.unknown;
    }


    /**
     * Sets the flags.
     * 
     * @param flags
     *            The flags to set
     */

    public void setUnknown(int flags)
    {
        this.unknown = flags;
    }


    /**
     * Returns the x.
     * 
     * @return The x
     */

    public int getX()
    {
        return this.x;
    }


    /**
     * Sets the x.
     * 
     * @param x
     *            The x to set
     */

    public void setX(int x)
    {
        this.x = x;
    }


    /**
     * Returns the y.
     * 
     * @return The y
     */

    public int getY()
    {
        return this.y;
    }


    /**
     * Sets the y.
     * 
     * @param y
     *            The y to set
     */

    public void setY(int y)
    {
        this.y = y;
    }


    /**
     * Returns the relative.
     *
     * @return The relative
     */
    
    public boolean isRelative()
    {
        return this.relative;
    }


    /**
     * Sets the relative.
     *
     * @param relative 
     *            The relative to set
     */
    
    public void setRelative(boolean relative)
    {
        this.relative = relative;
    }
}
