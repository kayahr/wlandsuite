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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.parts.SpecialActionTable;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * Transition action
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class TransitionAction implements Action
{
    /** If positioning is relative */
    private boolean relative;

    /** If transition must be confirmed by the user */
    private boolean confirm;

    /** The message to print */
    private int message;

    /** The target x position (relative or absolute) */
    private int targetX;

    /** The y position (relative or absolute) */
    private int targetY;

    /** The target map (255 means previous map) */
    private int targetMap;

    /** The new action class to set (255 means setting no new action) */
    private int newActionClass;

    /** The new action selector to set (255 means setting no new selector) */
    private int newAction;


    /**
     * Creates a new Transition Action by reading its data from the specified
     * input stream.
     * 
     * @param stream
     *            The input stream
     * @return The new Transition Action
     * @throws IOException
     */

    public static TransitionAction read(SeekableInputStream stream)
        throws IOException
    {
        TransitionAction action;
        int b;

        action = new TransitionAction();

        // Read first byte
        b = stream.readByte();
        action.relative = (b & 0x80) != 0;
        action.confirm = (b & 0x40) != 0;
        action.message = b & 0x3f;

        // Read the X position
        action.targetX = stream.readSignedByte();

        // Read the Y position
        action.targetY = stream.readSignedByte();

        // Read the map
        action.targetMap = stream.readByte();

        // Read the action class
        action.newActionClass = stream.readByte();

        // Read the action selector
        if (action.newActionClass < 253)
        {
            action.newAction = stream.readByte();
        }
        else
        {
            action.newAction = 255;
        }

        return action;
    }


    /**
     * Creates a new Transition Action by reading its data from the specified
     * XML element.
     * 
     * @param element
     *            The XML element
     * @return The new Transition Action
     */

    public static TransitionAction read(Element element)
    {
        TransitionAction action;

        action = new TransitionAction();
        action.relative = Boolean.parseBoolean(element.attributeValue(
            "relative", "false"));
        action.confirm = Boolean.parseBoolean(element.attributeValue("confirm",
            "false"));
        action.message = Integer.parseInt(element
            .attributeValue("message", "0"));
        action.targetX = Integer.parseInt(element
            .attributeValue("targetX", "0"));
        action.targetY = Integer.parseInt(element
            .attributeValue("targetY", "0"));
        action.targetMap = Integer.parseInt(element.attributeValue("targetMap",
            "255"));
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

        element = DocumentHelper.createElement("transition");
        element.addAttribute("id", Integer.toString(id));
        if (this.relative)
        {
            element.addAttribute("relative", this.relative ? "true" : "false");
        }
        if (this.confirm)
        {
            element.addAttribute("confirm", this.confirm ? "true" : "false");
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
        }
        if (this.targetX != 0)
        {
            element.addAttribute("targetX", Integer.toString(this.targetX));
        }
        if (this.targetY != 0)
        {
            element.addAttribute("targetY", Integer.toString(this.targetY));
        }
        if (this.targetMap != 255)
        {
            element.addAttribute("targetMap", Integer.toString(this.targetMap));
        }
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
        int b;

        b = this.relative ? 0x80 : 0;
        b |= this.confirm ? 0x40 : 0;
        b |= this.message & 0x3f;
        stream.writeByte(b);

        stream.writeSignedByte(this.targetX);
        stream.writeSignedByte(this.targetY);
        stream.writeByte(this.targetMap);
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
     * Returns the confirm flag.
     * 
     * @return The confirm flag
     */

    public boolean isConfirm()
    {
        return this.confirm;
    }


    /**
     * Sets the confirm flag.
     * 
     * @param confirm
     *            The confirm flag to set
     */

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }


    /**
     * Returns the relative flag.
     * 
     * @return The relative flag
     */

    public boolean isRelative()
    {
        return this.relative;
    }


    /**
     * Sets the relative flag.
     * 
     * @param relative
     *            The relative flag to set
     */

    public void setRelative(boolean relative)
    {
        this.relative = relative;
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
     * Returns the target map.
     * 
     * @return The target map
     */

    public int getTargetMap()
    {
        return this.targetMap;
    }


    /**
     * Sets the target map.
     * 
     * @param targetMap
     *            The target map to set
     */

    public void setTargetMap(int targetMap)
    {
        this.targetMap = targetMap;
    }


    /**
     * Returns the target X position.
     * 
     * @return The target X position
     */

    public int getTargetX()
    {
        return this.targetX;
    }


    /**
     * Sets the target X position.
     * 
     * @param targetX
     *            The target X postion to set
     */

    public void setTargetX(int targetX)
    {
        this.targetX = targetX;
    }


    /**
     * Returns the target Y position.
     * 
     * @return The target Y position
     */

    public int getTargetY()
    {
        return this.targetY;
    }


    /**
     * Sets the target Y position.
     * 
     * @param targetY
     *            The target Y position to set
     */

    public void setTargetY(int targetY)
    {
        this.targetY = targetY;
    }
}
