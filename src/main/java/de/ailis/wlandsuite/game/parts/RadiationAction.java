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

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * Radiation action
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class RadiationAction implements Action
{
    /** If armor is ignored */
    private boolean ignoreArmor;

    /** The id of the message to print */
    private int message;

    /** The damage (Number of d6 dices) */
    private int damage;

    /** The new action class to set (255 means setting no new action) */
    private int newActionClass;

    /** The new action selector to set (255 means setting no new selector) */
    private int newAction;


    /**
     * Creates a new Radiation Action by reading its data from the specified
     * input stream.
     * 
     * @param stream
     *            The input stream
     * @return The new Radiation Action
     * @throws IOException
     */

    public static RadiationAction read(InputStream stream) throws IOException
    {
        int b;
        RadiationAction action;

        action = new RadiationAction();

        // Read first byte
        b = stream.read();
        action.message = b;
        action.ignoreArmor = (b & 1) == 1;

        // Read the damage
        action.damage = stream.read();

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
     * Creates a new Radiation Action by reading the data from the specified XML
     * element
     * 
     * @param element
     *            The XML element
     * @return The Radiation Action
     */

    public static RadiationAction read(Element element)
    {
        RadiationAction action;

        action = new RadiationAction();

        action.ignoreArmor = Boolean.parseBoolean(element.attributeValue(
            "ignoreArmor", "false"));
        action.message = StringUtils.toInt(element
            .attributeValue("message", "0"));
        action.damage = StringUtils.toInt(element.attributeValue("damage", "0"));
        action.newActionClass = StringUtils.toInt(element.attributeValue(
            "class", "255"));
        action.newAction = StringUtils.toInt(element.attributeValue("selector",
            "255"));

        // Validate ignoreArmor flag
        if (action.ignoreArmor && (action.message & 1) == 0)
        {
            throw new GameException(
                "Invalid radiation data: Ignore armor flag can only be true when an odd message id is used");
        }
        if (!action.ignoreArmor && (action.message & 1) != 0)
        {
            throw new GameException(
                "Invalid radiation data: Ignore armor flag can only be false when an even message id is used");
        }

        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = XmlUtils.createElement("radiation");
        element.addAttribute("id", StringUtils.toHex(id));
        if (this.ignoreArmor)
        {
            element.addAttribute("ignoreArmor", "true");
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
        }
        if (this.damage != 0)
        {
            element.addAttribute("damage", Integer.toString(this.damage));
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
        stream.write(this.damage);
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
     * Returns the ignoreArmor flag
     * 
     * @return The ignorArmor flag
     */

    public boolean isIgnoreArmor()
    {
        return this.ignoreArmor;
    }


    /**
     * Sets the ignoreArmor flag.
     * 
     * @param ignoreArmor
     *            The ignoreArmor flag to set
     */

    public void setIgnoreArmor(boolean ignoreArmor)
    {
        this.ignoreArmor = ignoreArmor;
    }


    /**
     * Returns the message id.
     * 
     * @return The message id
     */

    public int getMessage()
    {
        return this.message;
    }


    /**
     * Sets the message id.
     * 
     * @param message
     *            The message id to set
     */

    public void setMessage(int message)
    {
        this.message = message;
    }


    /**
     * Returns the damage.
     * 
     * @return The damage
     */

    public int getDamage()
    {
        return this.damage;
    }


    /**
     * Sets the damage.
     * 
     * @param damage
     *            The damage to set
     */

    public void setTargetMap(int damage)
    {
        this.damage = damage;
    }
}
