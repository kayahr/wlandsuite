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

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * Alteration
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Alter
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
    private int newActionClass;

    /** The new action selector to set */
    private int newAction;


    /**
     * Creates a new alteration object by reading the data from the specified
     * stream.
     *
     * @param stream
     *            The stream to read the alteration data from
     * @param flags
     *            The flags
     * @return The alteration
     * @throws IOException
     *             When file operation fails.
     */

    public static Alter read(final SeekableInputStream stream, final int flags)
        throws IOException
    {
        Alter alteration;

        alteration = new Alter();
        alteration.unknown = flags & 0x7e;
        alteration.relative = (flags & 1) == 1;
        alteration.x = stream.readSignedByte();
        alteration.y = stream.readSignedByte();
        alteration.newActionClass = stream.readByte();
        alteration.newAction = stream.readByte();
        return alteration;
    }


    /**
     * Creates a new alteration object by reading the data from XML.
     *
     * @param element
     *            The XML element
     * @return The alteration.
     */

    public static Alter read(final Element element)
    {
        Alter alteration;

        alteration = new Alter();
        alteration.unknown = StringUtils.toInt(element.attributeValue("unknown",
            "0"));
        alteration.relative = Boolean.parseBoolean(element.attributeValue(
            "relative", "false"));
        alteration.x = StringUtils.toInt(element.attributeValue("x", "0"));
        alteration.y = StringUtils.toInt(element.attributeValue("y", "0"));
        alteration.newActionClass = StringUtils.toInt(element.attributeValue(
            "newActionClass", "255"));
        alteration.newAction = StringUtils.toInt(element.attributeValue(
            "newAction", "255"));
        return alteration;
    }


    /**
     * Converts the alteration data to XML and returns the XML element.
     *
     * @return The XML element
     */

    public Element toXml()
    {
        Element element;

        element = XmlUtils.createElement("alter");
        if (this.unknown != 0)
        {
            element.addAttribute("unknown", StringUtils.toHex(this.unknown));
        }
        if (this.relative)
        {
            element.addAttribute("relative", "true");
        }
        if (this.x != 0)
        {
            element.addAttribute("x", Integer.toString(this.x));
        }
        if (this.y != 0)
        {
            element.addAttribute("y", Integer.toString(this.y));
        }
        if (this.newActionClass != 255)
        {
            element.addAttribute("newActionClass", StringUtils.toHex(this.newActionClass));
        }
        if (this.newAction != 255)
        {
            element.addAttribute("newAction", StringUtils.toHex(this.newAction));
        }
        return element;
    }


    /**
     * Writes the alteration data to a stream.
     *
     * @param stream
     *            The stream to write to
     * @param last
     *            If this is the last alteration block
     */

    public void write(final SeekableOutputStream stream, final boolean last)
    {
        stream.writeByte(this.unknown | (last ? 128 : 0)
            | (this.relative ? 1 : 0));
        stream.writeSignedByte(this.x);
        stream.writeSignedByte(this.y);
        stream.writeByte(this.newActionClass);
        stream.writeByte(this.newAction);
    }


    /**
     * Returns the actionClass.
     *
     * @return The actionClass
     */

    public int getNewActionClass()
    {
        return this.newActionClass;
    }


    /**
     * Sets the actionClass.
     *
     * @param actionClass
     *            The actionClass to set
     */

    public void setNewActionClass(final int actionClass)
    {
        this.newActionClass = actionClass;
    }


    /**
     * Returns the actionSelector.
     *
     * @return The actionSelector
     */

    public int getNewAction()
    {
        return this.newAction;
    }


    /**
     * Sets the actionSelector.
     *
     * @param actionSelector
     *            The actionSelector to set
     */

    public void setNewAction(final int actionSelector)
    {
        this.newAction = actionSelector;
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

    public void setUnknown(final int flags)
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

    public void setX(final int x)
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

    public void setY(final int y)
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

    public void setRelative(final boolean relative)
    {
        this.relative = relative;
    }
}
