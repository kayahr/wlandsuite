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

    /** The new action class to apply when check succeeds */
    private int newActionClass = -1;

    /** The new action to apply when check succeeds */
    private int newAction = -1;


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
        int b;
        Check check;
        
        check = new Check();

        b = stream.read();
        if (b == 255) return null;
        check.type = b >> 5;
        check.difficulty = b & 31;
        check.value = stream.read();

        return check;
    }


    /**
     * Reads the replacement action class/action for this check.
     * 
     * @param stream
     *            The input stream
     * @throws IOException
     */

    public void readReplacement(InputStream stream) throws IOException
    {
        this.newActionClass = stream.read();
        this.newAction = stream.read();
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
     * Writes the replacement data of the check.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void writeReplacement(OutputStream stream) throws IOException
    {
        stream.write(this.newActionClass);
        stream.write(this.newAction);
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
        if (this.newActionClass != -1)
        {
            element.addAttribute("newActionClass", Integer.toString(this.newActionClass));
        }
        if (this.newAction != -1)
        {
            element.addAttribute("newAction", Integer.toString(this.newAction));
        }
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
        Check check;
        
        check = new Check();

        check.type = getType(element.getName());
        if (check.type == -1)
        {
            throw new GameException("Unknown check type: " + element.getName());
        }
        check.value = Integer.parseInt(element.attributeValue("value"));
        check.difficulty = Integer.parseInt(element.attributeValue("difficulty"));
        check.newActionClass = Integer.parseInt(element.attributeValue("newActionClass", "-1"));
        check.newAction = Integer.parseInt(element.attributeValue("newAction", "-1"));
        
        return check;
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


    /**
     * Returns the newAction.
     *
     * @return The newAction
     */
    
    public int getNewAction()
    {
        return this.newAction;
    }


    /**
     * Sets the newAction.
     *
     * @param newAction 
     *            The newAction to set
     */
    
    public void setNewAction(int newAction)
    {
        this.newAction = newAction;
    }


    /**
     * Returns the newActionClass.
     *
     * @return The newActionClass
     */
    
    public int getNewActionClass()
    {
        return this.newActionClass;
    }


    /**
     * Sets the newActionClass.
     *
     * @param newActionClass 
     *            The newActionClass to set
     */
    
    public void setNewActionClass(int newActionClass)
    {
        this.newActionClass = newActionClass;
    }


    /**
     * Sets the difficulty.
     *
     * @param difficulty 
     *            The difficulty to set
     */
    
    public void setDifficulty(int difficulty)
    {
        this.difficulty = difficulty;
    }


    /**
     * Sets the type.
     *
     * @param type 
     *            The type to set
     */
    
    public void setType(int type)
    {
        this.type = type;
    }


    /**
     * Sets the value.
     *
     * @param value 
     *            The value to set
     */
    
    public void setValue(int value)
    {
        this.value = value;
    }
}
