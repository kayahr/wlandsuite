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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The message action can output one or more strings and modify the current
 * square.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PrintAction implements Action
{
    /** The messages to print */
    private List<Integer> messages = new ArrayList<Integer>();

    /** The new action class to set (255 means setting no new action) */
    private int newActionClass;

    /** The new action selector to set (255 means setting no new selector) */
    private int newAction;


    /**
     * Constructor
     */

    public PrintAction()
    {
        this.messages = new ArrayList<Integer>();
    }


    /**
     * Creates and returns a new action by reading the data it from the
     * specified input stream.
     *
     * @param stream
     *            The input stream to read the action data from
     * @return The new action
     * @throws IOException
     *             When file operation fails.
     */

    public static PrintAction read(final InputStream stream) throws IOException
    {
        int b;
        PrintAction action;

        // Create new action object
        action = new PrintAction();

        // Read messages
        do
        {
            b = stream.read();
            action.messages.add(b & 0x7f);
        }
        while ((b & 0x80) == 0);

        // Read the next action class
        action.newActionClass = stream.read();

        // Read the next action selector
        if (action.newActionClass < 253)
        {
            action.newAction = stream.read();
        }
        else
        {
            action.newAction = 255;
        }

        // Return the action
        return action;
    }


    /**
     * Creates and returns a new action by reading the data from XML.
     *
     * @param element
     *            The XML element to read
     * @return The new action
     */

    public static PrintAction read(final Element element)
    {
        PrintAction action;

        // Create new message action
        action = new PrintAction();

        // Parse the data
        for (final Object item: element.elements("message"))
        {
            final Element subElement = (Element) item;
            action.messages.add(StringUtils.toInt(subElement.getTextTrim()));
        }

        action.newActionClass = StringUtils.toInt(element.attributeValue(
            "newActionClass", "255"));
        action.newAction = StringUtils.toInt(element.attributeValue("newAction",
            "255"));

        // Return the new action
        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(final int id)
    {
        Element element, subElement;

        element = XmlUtils.createElement("print");
        element.addAttribute("id", StringUtils.toHex(id));
        for (final int message: this.messages)
        {
            subElement = XmlUtils.createElement("message");
            subElement.setText(Integer.toString(message));
            element.add(subElement);
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

    public void write(final SeekableOutputStream stream,
        final SpecialActionTable specialActionTable)
    {
        for (int i = 0; i < this.messages.size() - 1; i++)
        {
            stream.write(this.messages.get(i));
        }
        stream
            .write(this.messages.get(this.messages.size() - 1).intValue() | 0x80);

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

    public void setNewActionClass(final int newActionClass)
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

    public void setNewAction(final int newAction)
    {
        this.newAction = newAction;
    }


    /**
     * Adds a new message
     *
     * @param message
     *            The message to add
     */

    public void addMessage(final int message)
    {
        this.messages.add(message);
    }


    /**
     * Returns the message with the specified index.
     *
     * @param index
     *            The index
     * @return The message
     */

    public int getMessage(final int index)
    {
        return this.messages.get(index);
    }


    /**
     * Sets a message at a specific index.
     *
     * @param index
     *            The index
     * @param message
     *            The message
     */

    public void setMessage(final int index, final int message)
    {
        this.messages.set(index, message);
    }


    /**
     * Deletes all messages
     */

    public void clearMessages()
    {
        this.messages.clear();
    }


    /**
     * Removes the message at the specified index.
     *
     * @param index
     *            The index
     */

    public void removeMessage(final int index)
    {
        this.messages.remove(index);
    }


    /**
     * Returns the number of messages.
     *
     * @return The number of messages
     */

    public int countMessages()
    {
        return this.messages.size();
    }


    /**
     * Inserts a message at a specific index.
     *
     * @param index
     *            The index
     * @param message
     *            The message
     */

    public void addMessage(final int index, final int message)
    {
        this.messages.add(index, message);
    }
}
