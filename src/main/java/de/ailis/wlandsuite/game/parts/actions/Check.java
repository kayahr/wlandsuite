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

package de.ailis.wlandsuite.game.parts.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.rawgame.GameException;


/**
 * A check used in the Check Action.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Check
{
    /** Constant for skill check */
    public static final int TYPE_SKILL = 0;

    /** Constant for item check */
    public static final int TYPE_ITEM = 1;

    /** Constant for attribute check */
    public static final int TYPE_ATTRIBUTE = 2;

    /** Constant for party member check */
    public static final int TYPE_MEMBERS = 3;

    /** Constant for party member check */
    public static final int TYPE_UNKNOWN4 = 4;

    /** Constant for party member check */
    public static final int TYPE_UNKNOWN5 = 5;

    /** Constant for party member check */
    public static final int TYPE_UNKNOWN6 = 6;

    /** Constant for party member check */
    public static final int TYPE_UNKNOWN7 = 7;

    /** The XML names for the different check types */
    private static final String xmlNames[] = { "skill", "item", "attribute",
        "members", "unknown4", "unknown5", "unknown6", "unknown7" };

    /** The check type. One of the TYPE_* constants */
    private int type;

    /** The check value (For example the item to check for) */
    private int value;

    /** The difficulty */
    private int difficulty;


    /**
     * Constructor
     * 
     * @param type
     *            The check type
     * @param difficulty
     *            The difficulty
     * @param value
     *            The check value
     */

    public Check(int type, int value, int difficulty)
    {
        this.type = type;
        this.value = value;
        this.difficulty = difficulty;
    }


    /**
     * Returns the check value.
     * 
     * @return The check value
     */

    public int getValue()
    {
        return this.value;
    }


    /**
     * Returns the difficulty.
     * 
     * @return The difficulty
     */

    public int getDifficulty()
    {
        return this.difficulty;
    }


    /**
     * Returns the type.
     * 
     * @return The type
     */

    public int getType()
    {
        return this.type;
    }


    /**
     * Creates and returns a new check by reading its data from the specified
     * stream. If no more checks are on the stream then null is returned.
     * 
     * @param stream
     *            The input stream
     * @return The check
     * @throws IOException
     */

    public static Check read(InputStream stream) throws IOException
    {
        int type, value, difficulty, b;

        b = stream.read();
        if (b == 255) return null;
        type = b >> 5;
        difficulty = b & 31;
        value = stream.read();

        return new Check(type, value, difficulty);

    }


    /**
     * Writes the check data to the specified stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        stream.write(this.type << 5 | (this.difficulty & 31));
        stream.write(this.value);
    }


    /**
     * Returns the check data as XML.
     * 
     * @return The check data as XML
     */

    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement(getXmlName(this.type));
        element.addAttribute("value", Integer.toString(this.value));
        element.addAttribute("difficulty", Integer.toString(this.difficulty));
        return element;
    }


    /**
     * Creates and returns a new Check object by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The check data
     */

    public static Check read(Element element)
    {
        int type, difficulty, value;

        type = getType(element.getName());
        if (type == -1)
        {
            throw new GameException("Unknown check type: " + element.getName());
        }
        value = Integer.parseInt(element.attributeValue("value"));
        difficulty = Integer.parseInt(element.attributeValue("difficulty"));
        return new Check(type, value, difficulty);
    }


    /**
     * Returns the type for the specified xml name. Returns -1 if no type was
     * found.
     * 
     * @param xmlName
     *            The XML name
     * @return The type
     */

    private static int getType(String xmlName)
    {
        for (int i = 0; i < xmlNames.length; i++)
        {
            if (xmlNames[i].equals(xmlName))
            {
                return i;
            }
        }
        return -1;
    }


    /**
     * Returns the XML name for the specified check type.
     * 
     * @param type
     *            The check type
     * @return The XML name
     */

    private static String getXmlName(int type)
    {
        return xmlNames[type];
    }
}
