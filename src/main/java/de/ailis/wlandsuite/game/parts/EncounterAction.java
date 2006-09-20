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

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * The encounter action is used for fixed (class 3) and random (class 15)
 * encounters.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class EncounterAction implements Action
{
    /** The minimum distance in feet for the encounter to start */
    private int visibleDistance;

    /** The minimum hit distance for the monsters in the encounter */
    private int hitDistance;

    /** The message to print when the encounter begins */
    private int message;

    /** The monster type of the first group */
    private int monster1;

    /** If the number of monsters in the first group is random */
    private boolean random1;

    /** The number of monsters in the first group */
    private int maxGroupSize1;

    /** The monster type of the second group */
    private int monster2;

    /** If the number of monsters in the second group is random */
    private boolean random2;

    /** The number of monsters in the second group */
    private int maxGroupSize2;

    /** The monster type of the third group */
    private int monster3;

    /** If the number of monsters in the third group is random */
    private boolean random3;

    /** The number of monsters in the third group */
    private int maxGroupSize3;

    /** If the proper name should be displayed instead of a generic one */
    private boolean properName;

    /** If the monster is friendly until attacked */
    private boolean friendly;

    /** Unknown bit 3 in byte 09 */
    private boolean unknown093;

    /** The NPC number (0 if not hireable) */
    private int npc;

    /** The new action class to set after the encounter */
    private int newActionClass;

    /** The action class to set after the encounter */
    private int newAction;


    /**
     * Creates and returns a new Encounter Action by reading its data from the
     * specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The new Encounter Action
     * @throws IOException
     */

    public static EncounterAction read(InputStream stream) throws IOException
    {
        EncounterAction action;
        int b;

        action = new EncounterAction();

        action.visibleDistance = stream.read();
        action.hitDistance = stream.read();
        action.message = stream.read();
        action.monster1 = stream.read();
        b = stream.read();
        action.random1 = (b & 128) == 128;
        action.maxGroupSize1 = b & 127;
        action.monster2 = stream.read();
        b = stream.read();
        action.random2 = (b & 128) == 128;
        action.maxGroupSize2 = b & 127;
        action.monster3 = stream.read();
        b = stream.read();
        action.random3 = (b & 128) == 128;
        action.maxGroupSize3 = b & 127;
        b = stream.read();
        action.properName = (b & 1) == 1;
        action.friendly = (b & 2) == 2;
        action.unknown093 = (b & 4) == 4;
        if ((b & 8) == 8)
        {
            throw new GameException("unknown094 is set!");
        }
        action.npc = b >> 4;

        action.newActionClass = stream.read();
        action.newAction = stream.read();

        return action;
    }


    /**
     * Creates and returns an Encounter Action by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The Encounter Action
     */

    public static EncounterAction read(Element element)
    {
        EncounterAction action;

        action = new EncounterAction();

        action.visibleDistance = Integer.parseInt(element.attributeValue(
            "visibleDistance", "0"));
        action.hitDistance = Integer.parseInt(element.attributeValue(
            "hitDistance", "0"));
        action.message = Integer.parseInt(element
            .attributeValue("message", "0"));
        action.monster1 = Integer.parseInt(element.attributeValue("monster1",
            "0"));
        action.maxGroupSize1 = Integer.parseInt(element.attributeValue(
            "maxGroupSize1", "0"));
        action.random1 = Boolean.parseBoolean(element.attributeValue("random1",
            "false"));
        action.monster2 = Integer.parseInt(element.attributeValue("monster2",
            "0"));
        action.maxGroupSize2 = Integer.parseInt(element.attributeValue(
            "maxGroupSize2", "0"));
        action.random2 = Boolean.parseBoolean(element.attributeValue("random2",
            "false"));
        action.monster3 = Integer.parseInt(element.attributeValue("monster3",
            "0"));
        action.maxGroupSize3 = Integer.parseInt(element.attributeValue(
            "maxGroupSize3", "0"));
        action.random3 = Boolean.parseBoolean(element.attributeValue("random3",
            "false"));
        action.properName = Boolean.parseBoolean(element.attributeValue(
            "properName", "false"));
        action.friendly = Boolean.parseBoolean(element.attributeValue(
            "friendly", "false"));
        action.unknown093 = Boolean.parseBoolean(element.attributeValue(
            "unknown093", "false"));
        action.npc = Integer.parseInt(element.attributeValue("npc", "0"));
        action.newActionClass = Integer.parseInt(element.attributeValue(
            "newActionClass", "255"));
        action.newAction = Integer.parseInt(element.attributeValue("newAction",
            "255"));

        // Return the check action
        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = XMLUtils.createElement("encounter");
        element.addAttribute("id", Integer.toString(id));
        if (this.visibleDistance != 0)
        {
            element.addAttribute("visibleDistance", Integer
                .toString(this.visibleDistance));
        }
        if (this.hitDistance != 0)
        {
            element.addAttribute("hitDistance", Integer
                .toString(this.hitDistance));
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
        }
        if (this.monster1 != 0)
        {
            element.addAttribute("monster1", Integer.toString(this.monster1));
        }
        if (this.maxGroupSize1 != 0)
        {
            element.addAttribute("maxGroupSize1", Integer
                .toString(this.maxGroupSize1));
        }
        if (this.random1)
        {
            element.addAttribute("random1", this.random1 ? "true" : "false");
        }
        if (this.monster2 != 0)
        {
            element.addAttribute("monster2", Integer.toString(this.monster2));
        }
        if (this.maxGroupSize2 != 0)
        {
            element.addAttribute("maxGroupSize2", Integer
                .toString(this.maxGroupSize2));
        }
        if (this.random2)
        {
            element.addAttribute("random2", this.random2 ? "true" : "false");
        }
        if (this.monster3 != 0)
        {
            element.addAttribute("monster3", Integer.toString(this.monster3));
        }
        if (this.maxGroupSize3 != 0)
        {
            element.addAttribute("maxGroupSize3", Integer
                .toString(this.maxGroupSize3));
        }
        if (this.random3)
        {
            element.addAttribute("random3", this.random3 ? "true" : "false");
        }
        if (this.properName)
        {
            element.addAttribute("properName", this.properName ? "true"
                : "false");
        }
        if (this.friendly)
        {
            element.addAttribute("friendly", this.friendly ? "true" : "false");
        }
        if (this.unknown093)
        {
            element.addAttribute("unknown093", this.unknown093 ? "true"
                : "false");
        }
        if (this.npc != 0)
        {
            element.addAttribute("npc", Integer.toString(this.npc));
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
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable)
    {
        stream.write(this.visibleDistance);
        stream.write(this.hitDistance);
        stream.write(this.message);
        stream.write(this.monster1);
        stream.write((this.maxGroupSize1 & 127) | (this.random1 ? 128 : 0));
        stream.write(this.monster2);
        stream.write((this.maxGroupSize2 & 127) | (this.random2 ? 128 : 0));
        stream.write(this.monster3);
        stream.write((this.maxGroupSize3 & 127) | (this.random3 ? 128 : 0));
        stream.write((this.npc << 4) | (this.properName ? 1 : 0)
            | (this.friendly ? 2 : 0) | (this.unknown093 ? 4 : 0));
        stream.write(this.newActionClass);
        stream.write(this.newAction);
    }


    /**
     * Returns the friendly.
     * 
     * @return The friendly
     */

    public boolean isFriendly()
    {
        return this.friendly;
    }


    /**
     * Sets the friendly.
     * 
     * @param friendly
     *            The friendly to set
     */

    public void setFriendly(boolean friendly)
    {
        this.friendly = friendly;
    }


    /**
     * Returns the hitDistance.
     * 
     * @return The hitDistance
     */

    public int getHitDistance()
    {
        return this.hitDistance;
    }


    /**
     * Sets the hitDistance.
     * 
     * @param hitDistance
     *            The hitDistance to set
     */

    public void setHitDistance(int hitDistance)
    {
        this.hitDistance = hitDistance;
    }


    /**
     * Returns the maxGroupSize1.
     * 
     * @return The maxGroupSize1
     */

    public int getMaxGroupSize1()
    {
        return this.maxGroupSize1;
    }


    /**
     * Sets the maxGroupSize1.
     * 
     * @param maxGroupSize1
     *            The maxGroupSize1 to set
     */

    public void setMaxGroupSize1(int maxGroupSize1)
    {
        this.maxGroupSize1 = maxGroupSize1;
    }


    /**
     * Returns the maxGroupSize2.
     * 
     * @return The maxGroupSize2
     */

    public int getMaxGroupSize2()
    {
        return this.maxGroupSize2;
    }


    /**
     * Sets the maxGroupSize2.
     * 
     * @param maxGroupSize2
     *            The maxGroupSize2 to set
     */

    public void setMaxGroupSize2(int maxGroupSize2)
    {
        this.maxGroupSize2 = maxGroupSize2;
    }


    /**
     * Returns the maxGroupSize3.
     * 
     * @return The maxGroupSize3
     */

    public int getMaxGroupSize3()
    {
        return this.maxGroupSize3;
    }


    /**
     * Sets the maxGroupSize3.
     * 
     * @param maxGroupSize3
     *            The maxGroupSize3 to set
     */

    public void setMaxGroupSize3(int maxGroupSize3)
    {
        this.maxGroupSize3 = maxGroupSize3;
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
     * Returns the monster1.
     * 
     * @return The monster1
     */

    public int getMonster1()
    {
        return this.monster1;
    }


    /**
     * Sets the monster1.
     * 
     * @param monster1
     *            The monster1 to set
     */

    public void setMonster1(int monster1)
    {
        this.monster1 = monster1;
    }


    /**
     * Returns the monster2.
     * 
     * @return The monster2
     */

    public int getMonster2()
    {
        return this.monster2;
    }


    /**
     * Sets the monster2.
     * 
     * @param monster2
     *            The monster2 to set
     */

    public void setMonster2(int monster2)
    {
        this.monster2 = monster2;
    }


    /**
     * Returns the monster3.
     * 
     * @return The monster3
     */

    public int getMonster3()
    {
        return this.monster3;
    }


    /**
     * Sets the monster3.
     * 
     * @param monster3
     *            The monster3 to set
     */

    public void setMonster3(int monster3)
    {
        this.monster3 = monster3;
    }


    /**
     * Returns the npc.
     * 
     * @return The npc
     */

    public int getNpc()
    {
        return this.npc;
    }


    /**
     * Sets the npc.
     * 
     * @param npc
     *            The npc to set
     */

    public void setNpc(int npc)
    {
        this.npc = npc;
    }


    /**
     * Returns the properName.
     * 
     * @return The properName
     */

    public boolean isProperName()
    {
        return this.properName;
    }


    /**
     * Sets the properName.
     * 
     * @param properName
     *            The properName to set
     */

    public void setProperName(boolean properName)
    {
        this.properName = properName;
    }


    /**
     * Returns the random1.
     * 
     * @return The random1
     */

    public boolean isRandom1()
    {
        return this.random1;
    }


    /**
     * Sets the random1.
     * 
     * @param random1
     *            The random1 to set
     */

    public void setRandom1(boolean random1)
    {
        this.random1 = random1;
    }


    /**
     * Returns the random2.
     * 
     * @return The random2
     */

    public boolean isRandom2()
    {
        return this.random2;
    }


    /**
     * Sets the random2.
     * 
     * @param random2
     *            The random2 to set
     */

    public void setRandom2(boolean random2)
    {
        this.random2 = random2;
    }


    /**
     * Returns the random3.
     * 
     * @return The random3
     */

    public boolean isRandom3()
    {
        return this.random3;
    }


    /**
     * Sets the random3.
     * 
     * @param random3
     *            The random3 to set
     */

    public void setRandom3(boolean random3)
    {
        this.random3 = random3;
    }


    /**
     * Returns the unknown093.
     * 
     * @return The unknown093
     */

    public boolean isUnknown093()
    {
        return this.unknown093;
    }


    /**
     * Sets the unknown093.
     * 
     * @param unknown093
     *            The unknown093 to set
     */

    public void setUnknown093(boolean unknown093)
    {
        this.unknown093 = unknown093;
    }


    /**
     * Returns the visibleDistance.
     * 
     * @return The visibleDistance
     */

    public int getVisibleDistance()
    {
        return this.visibleDistance;
    }


    /**
     * Sets the visibleDistance.
     * 
     * @param visibleDistance
     *            The visibleDistance to set
     */

    public void setVisibleDistance(int visibleDistance)
    {
        this.visibleDistance = visibleDistance;
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
}
