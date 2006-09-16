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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.parts.SpecialActionTable;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * Impassable action
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ImpassableAction implements Action
{
    /** The message to print */
    private int message;

    /** The new action class to set (255 means setting no new action) */
    private int newActionClass;

    /** The new action selector to set (255 means setting no new selector) */
    private int newAction;


    /**
     * Creates a new Impassable Action by reading the data from a stream.
     * 
     * @param stream
     *            The input stream
     * @return The Impassable Action
     * @throws IOException
     */

    public static ImpassableAction read(InputStream stream) throws IOException
    {
        ImpassableAction action;

        action = new ImpassableAction();

        // Read the message
        action.message = stream.read();

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
     * Creates a new Impassable Action by reading the data from XML.
     * 
     * @param element
     *            The XML element
     * @return The new Impassable Action
     */

    public static ImpassableAction read(Element element)
    {
        ImpassableAction action;

        action = new ImpassableAction();

        action.message = Integer.parseInt(element.attributeValue("message"));
        action.newActionClass = Integer.parseInt(element.attributeValue(
            "newActionClass", "255"));
        action.newAction = Integer.parseInt(element.attributeValue("newAction",
            "255"));

        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.actions.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = DocumentHelper.createElement("impassable");
        element.addAttribute("id", Integer.toString(id));
        element.addAttribute("message", Integer.toString(this.message));
        if (this.newActionClass != 255)
        {
            element.addAttribute("newActionClass", Integer
                .toString(this.newActionClass));
        }
        if (this.newAction != 255)
        {
            element.addAttribute("newAction", Integer.toString(this.newAction));
        }
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.actions.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable)
    {
        stream.writeByte(this.message);
        stream.writeByte(this.newActionClass);
        if (this.newActionClass < 253)
        {
            stream.writeByte(this.newAction);
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
     * @param actionClass
     *            The action class to set
     */

    public void setNewActionClass(int actionClass)
    {
        this.newActionClass = actionClass;
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
     * @param actionSelector
     *            The action selector to set
     */

    public void setNewAction(int actionSelector)
    {
        this.newAction = actionSelector;
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
}
