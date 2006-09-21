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

import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * The mask action is used to mask a square with a different tile. It also can
 * output a message and it change the square when the player steps on it.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MaskAction implements Action
{
    /** The message to print */
    private int message;

    /** If the square is impassable */
    private boolean impassable;

    /** The tile to display */
    private int tile;

    /** The new action class to set (255 means setting no new action) */
    private int newActionClass;

    /** The new action selector to set (255 means setting no new selector) */
    private int newAction;


    /**
     * Creates and returns a new Mask Action by reading the data from the
     * specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The Mask Action
     * @throws IOException
     */

    public static MaskAction read(InputStream stream) throws IOException
    {
        MaskAction action;
        int b;

        action = new MaskAction();

        action.message = stream.read();
        b = stream.read();
        action.impassable = (b & 128) == 128;
        action.tile = b & 127;

        // Read the action class
        action.newActionClass = stream.read();

        // Read the action selector
        if (action.newActionClass < 253)
        {
            action.newAction = stream.read();
        }
        else
        {
            action.newAction = 255;
        }

        return action;
    }


    /**
     * Creates and returns a new Mask Action by reading its data from the
     * specified XML element.
     * 
     * @param element
     *            The XML element
     * @return The Mask Action
     */

    public static MaskAction read(Element element)
    {
        MaskAction action;

        action = new MaskAction();

        action.message = StringUtils.toInt(element.attributeValue("message", "0"));
        action.tile = StringUtils.toInt(element.attributeValue("tile"));
        action.impassable = Boolean.parseBoolean(element
            .attributeValue("impassable", "false"));
        action.newActionClass = StringUtils.toInt(element.attributeValue(
            "newActionClass", "255"));
        action.newAction = StringUtils.toInt(element.attributeValue("newAction",
            "255"));

        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = XmlUtils.createElement("mask");
        element.addAttribute("id", StringUtils.toHex(id));
        element.addAttribute("tile", Integer.toString(this.tile));
        if (this.impassable)
        {
            element.addAttribute("impassable", "true");
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
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
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable)
    {
        stream.write(this.message);
        stream.write((this.tile & 127) | (this.impassable ? 128 : 0));
        stream.write(this.newActionClass);
        if (this.newActionClass < 253)
        {
            stream.write(this.newAction);
        }
    }


    /**
     * Returns the action class.
     * 
     * @return The action class
     */

    public int getNewActionClass()
    {
        return this.newActionClass;
    }


    /**
     * Sets the action class.
     * 
     * @param newActionClass
     *            The action class to set
     */

    public void setNewActionClass(int newActionClass)
    {
        this.newActionClass = newActionClass;
    }


    /**
     * Returns the action selector.
     * 
     * @return The action selector
     */

    public int getNewAction()
    {
        return this.newAction;
    }


    /**
     * Sets the action selector.
     * 
     * @param newAction
     *            The action selector to set
     */

    public void setNewAction(int newAction)
    {
        this.newAction = newAction;
    }


    /**
     * Returns the message.
     * 
     * @return The message
     */

    public int getMessage()
    {
        return this.message;
    }


    /**
     * Sets the message.
     * 
     * @param message
     *            The message to set
     */

    public void setMessage(int message)
    {
        this.message = message;
    }


    /**
     * Returns the impassable.
     * 
     * @return The impassable
     */

    public boolean isImpassable()
    {
        return this.impassable;
    }


    /**
     * Sets the impassable.
     * 
     * @param impassable
     *            The impassable to set
     */

    public void setImpassable(boolean impassable)
    {
        this.impassable = impassable;
    }


    /**
     * Returns the tile.
     * 
     * @return The tile
     */

    public int getTile()
    {
        return this.tile;
    }


    /**
     * Sets the tile.
     * 
     * @param tile
     *            The tile to set
     */

    public void setTile(int tile)
    {
        this.tile = tile;
    }
}
