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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.parts.SpecialActionTable;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * Alter code
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class AlterAction implements Action
{
    /** The message to print when player enters the square */
    private int message;

    /** The alterations */
    private List<Alteration> alterations = new ArrayList<Alteration>();

    /** The new action class to set (255 means setting no new action) */
    private int newActionClass;

    /** The new action selector to set (255 means setting no new selector) */
    private int newAction;


    /**
     * Creates a new Alter Action by reading the data from the specified input
     * stream.
     * 
     * @param stream
     *            The input stream
     * @return The new Alter Action
     * @throws IOException
     */

    public static AlterAction read(SeekableInputStream stream)
        throws IOException
    {
        int b;
        AlterAction action;

        action = new AlterAction();

        // Read the message
        action.message = stream.read();

        // Read alterations
        do
        {
            b = stream.read();
            action.alterations.add(Alteration.read(stream, b & 0x7f));
        }
        while ((b & 0x80) == 0);

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
        
        // Bugfix for Alter-check on map 8 of game1
        if (action.message == 0 && action.newAction == 0 && action.newActionClass == 0 && action.alterations.size() == 13 && action.alterations.get(4).getUnknown() == 2 && action.alterations.get(12).getUnknown() == 78)
        {
            System.out.println("Patching alter-check (7) on map 8");
            action.newActionClass = action.alterations.get(4).getUnknown();
            action.newAction = action.alterations.get(4).getX();
            while (action.alterations.size() != 4)
            {
                action.alterations.remove(action.alterations.size() - 1);
            }                
        }

        return action;
    }


    /**
     * Creates a new Alter Action from XML.
     * 
     * @param element
     *            The XML element
     * @return The new Alter Action
     */

    public static AlterAction read(Element element)
    {
        AlterAction action;

        action = new AlterAction();

        action.message = Integer.parseInt(element.attributeValue("message"));
        action.newActionClass = Integer.parseInt(element.attributeValue(
            "newActionClass", "255"));
        action.newAction = Integer.parseInt(element.attributeValue("newAction",
            "255"));
        for (Object item: element.elements("alteration"))
        {
            Element subElement = (Element) item;
            action.alterations.add(Alteration.read(subElement));
        }

        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.actions.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = DocumentHelper.createElement("alter");
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
        for (Alteration alteration: this.alterations)
        {
            element.add(alteration.toXml());
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
        stream.write(this.message);
        for (int i = 0; i < this.alterations.size(); i++)
        {
            this.alterations.get(i).write(stream,
                i == this.alterations.size() - 1);
        }
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
     * Returns the alterations.
     * 
     * @return The alterations
     */

    public List<Alteration> getAlterations()
    {
        return this.alterations;
    }


    /**
     * Sets the alterations.
     * 
     * @param alterations
     *            The alterations to set
     */

    public void setAlterations(List<Alteration> alterations)
    {
        this.alterations = alterations;
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
