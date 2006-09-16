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
 * The check action defines a skill/check/attribute/... check.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CheckAction implements Action
{
    /** The flags */
    private int flags;

    /** The message to display when entering the square */
    private int startMessage;

    /** The message to display if the check passed */
    private int passMessage;

    /** The message to display if the check failed */
    private int failMessage;

    /**
     * The new action class to set when the check passed (255 means setting no
     * new action class)
     */
    private int passNewActionClass;

    /**
     * The new action to set when the check passed (255 means setting no new
     * action)
     */
    private int passNewAction;

    /**
     * The new action class to set when the check failed (255 means setting no
     * new action class)
     */
    private int failNewActionClass;

    /**
     * The new action to set when the check failed (255 means setting no new
     * action)
     */
    private int failNewAction;

    /** The unknown byte at position 08 */
    private int unknown08;

    /** The unknown byte at position 09 */
    private int unknown09;

    /** The checks */
    private List<Check> checks;


    /**
     * Constructor
     */

    public CheckAction()
    {
        this.checks = new ArrayList<Check>();
    }


    /**
     * Creates and returns a new Check Action by reading its data from the
     * specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The new Check Action
     * @throws IOException
     */

    public static CheckAction read(SeekableInputStream stream)
        throws IOException
    {
        CheckAction action;
        Check check;

        action = new CheckAction();

        action.flags = stream.readByte();
        action.startMessage = stream.readByte();
        action.passMessage = stream.readByte();
        action.failMessage = stream.readByte();
        action.passNewActionClass = stream.readByte();
        action.passNewAction = stream.readByte();
        action.failNewActionClass = stream.readByte();
        action.failNewAction = stream.readByte();
        action.unknown08 = stream.readByte();
        action.unknown09 = stream.readByte();

        while ((check = Check.read(stream)) != null)
        {
            action.checks.add(check);
        }

        // Bugfix for Safe-check on map 3 of game1
        if (action.flags == 64 && action.startMessage == 31
            && action.passMessage == 32 && action.failMessage == 0
            && action.passNewActionClass == 2 && action.passNewAction == 35
            && action.failNewActionClass == 2 && action.failNewAction == 35
            && action.unknown08 == 0 && action.unknown09 == 0
            && action.checks.size() > 2
            && action.checks.get(2).getType() == Check.TYPE_UNKNOWN6)
        {
            System.out.println("Patching safe-check (5) on map 3");
            action.checks.remove(3);
            action.checks.remove(2);
        }

        // Bugfix for Safe-check on map 4 of game1
        if (action.flags == 228 && action.startMessage == 65
            && action.passMessage == 99 && action.failMessage == 0
            && action.passNewActionClass == 8 && action.passNewAction == 5
            && action.failNewActionClass == 255 && action.failNewAction == 0
            && action.unknown08 == 0 && action.unknown09 == 4
            && action.checks.size() > 2
            && action.checks.get(2).getType() == Check.TYPE_UNKNOWN7)
        {
            System.out.println("Patching safe-check (20) on map 4");
            action.checks.remove(9);
            action.checks.remove(8);
            action.checks.remove(7);
            action.checks.remove(6);
            action.checks.remove(5);
            action.checks.remove(4);
            action.checks.remove(3);
            action.checks.remove(2);
        }

        // Bugfix for Barrier-check on map 7 of game2
        if (action.flags == 0 && action.startMessage == 36
            && action.passMessage == 0 && action.failMessage == 0
            && action.passNewActionClass == 255 && action.passNewAction == 255
            && action.failNewActionClass == 255 && action.failNewAction == 255
            && action.unknown08 == 0 && action.unknown09 == 0
            && action.checks.size() > 2
            && action.checks.get(2).getType() == Check.TYPE_UNKNOWN4)
        {
            System.out.println("Patching barrier-check (15) on map 4");
            action.checks.clear();
            action.checks.add(new Check(Check.TYPE_SKILL, 0, 0));
        }

        return action;
    }

    /**
     * Creates and returns a Check Action by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The Check Action
     */

    public static CheckAction read(Element element)
    {
        CheckAction action;

        action = new CheckAction();

        action.flags = Integer.parseInt(element.attributeValue("flags"));
        action.startMessage = Integer.parseInt(element
            .attributeValue("startMessage"));
        action.passMessage = Integer.parseInt(element
            .attributeValue("passMessage"));
        action.failMessage = Integer.parseInt(element
            .attributeValue("failMessage"));
        action.passNewActionClass = Integer.parseInt(element
            .attributeValue("passNewActionClass"));
        action.passNewAction = Integer.parseInt(element
            .attributeValue("passNewAction"));
        action.failNewActionClass = Integer.parseInt(element
            .attributeValue("failNewActionClass"));
        action.failNewAction = Integer.parseInt(element
            .attributeValue("failNewAction"));
        action.unknown08 = Integer
            .parseInt(element.attributeValue("unknown08"));
        action.unknown09 = Integer
            .parseInt(element.attributeValue("unknown09"));
        // action.bugfix = Integer.parseInt(element.attributeValue("bugfix",
        // "0"));

        // Read the checks
        for (Object check: element.elements())
        {
            Element subElement;

            subElement = (Element) check;
            action.checks.add(Check.read(subElement));
        }

        // Return the check action
        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.actions.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = DocumentHelper.createElement("check");
        element.addAttribute("id", Integer.toString(id));
        element.addAttribute("flags", Integer.toString(this.flags));
        element.addAttribute("startMessage", Integer
            .toString(this.startMessage));
        element.addAttribute("passMessage", Integer.toString(this.passMessage));
        element.addAttribute("failMessage", Integer.toString(this.failMessage));
        element.addAttribute("passNewActionClass", Integer
            .toString(this.passNewActionClass));
        element.addAttribute("passNewAction", Integer
            .toString(this.passNewAction));
        element.addAttribute("failNewActionClass", Integer
            .toString(this.failNewActionClass));
        element.addAttribute("failNewAction", Integer
            .toString(this.failNewAction));
        element.addAttribute("unknown08", Integer.toString(this.unknown08));
        element.addAttribute("unknown09", Integer.toString(this.unknown09));
        /*
         * if (this.bugfix != 0) { element.addAttribute("bugfix",
         * Integer.toString(this.bugfix)); }
         */

        for (Check check: this.checks)
        {
            element.add(check.toXml());
        }

        return element;
    }


    /**
     * @throws IOException
     * @see de.ailis.wlandsuite.game.parts.actions.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        stream.write(this.flags);
        stream.write(this.startMessage);
        stream.write(this.passMessage);
        stream.write(this.failMessage);
        stream.write(this.passNewActionClass);
        stream.write(this.passNewAction);
        stream.write(this.failNewActionClass);
        stream.write(this.failNewAction);
        /*
         * if (this.bugfix != 2) { bitStream.writeByte(this.failActionSelector); }
         */
        stream.write(this.unknown08);
        stream.write(this.unknown09);

        for (Check check: this.checks)
        {
            check.write(stream);
        }
        stream.write(255);

        /*
         * if (this.bugfix != 3) { for (byte b: this.checks) {
         * bitStream.writeByte(b & 0xff); } } else { bitStream.writeByte(0); }
         * if (this.bugfix != 1) { bitStream.writeByte(0xff); }
         */
    }


    /**
     * Returns the start message.
     * 
     * @return The start message
     */

    public int getStartMessage()
    {
        return this.startMessage;
    }


    /**
     * Sets the start message.
     * 
     * @param startMessage
     *            The start message to set
     */

    public void setStartMessage(int startMessage)
    {
        this.startMessage = startMessage;
    }


    /**
     * Returns the new action class to set when the check failes.
     * 
     * @return The action class
     */

    public int getFailNewActionClass()
    {
        return this.failNewActionClass;
    }


    /**
     * Sets the new action class to set when the check failes.
     * 
     * @param failNewActionClass
     *            The action class to set
     */

    public void setFailNewActionClass(int failNewActionClass)
    {
        this.failNewActionClass = failNewActionClass;
    }


    /**
     * Returns the new action to set when the check failes.
     * 
     * @return The action
     */

    public int getFailNewAction()
    {
        return this.failNewAction;
    }


    /**
     * Sets the new action to set when the check failes.
     * 
     * @param failNewAction
     *            The action to set
     */

    public void setFailNewAction(int failNewAction)
    {
        this.failNewAction = failNewAction;
    }


    /**
     * Returns the fail message.
     * 
     * @return The fail message
     */

    public int getFailMessage()
    {
        return this.failMessage;
    }


    /**
     * Sets the fail message.
     * 
     * @param failMessage
     *            The fail message to set
     */

    public void setFailMessage(int failMessage)
    {
        this.failMessage = failMessage;
    }


    /**
     * Returns the flags.
     * 
     * @return The flags
     */

    public int getFlags()
    {
        return this.flags;
    }


    /**
     * Sets the flags.
     * 
     * @param flags
     *            The flags to set
     */

    public void setFlags(int flags)
    {
        this.flags = flags;
    }


    /**
     * Returns the new action class to set when the check passes.
     * 
     * @return The action class
     */

    public int getPassNewActionClass()
    {
        return this.passNewActionClass;
    }


    /**
     * Sets the new action class to set when the check passes.
     * 
     * @param passNewActionClass
     *            The action class to set
     */

    public void setPassNewActionClass(int passNewActionClass)
    {
        this.passNewActionClass = passNewActionClass;
    }


    /**
     * Returns the new action to set when the check passes.
     * 
     * @return The action
     */

    public int getPassNewAction()
    {
        return this.passNewAction;
    }


    /**
     * Sets the new action to set when the check passes.
     * 
     * @param passNewAction
     *            The action to set
     */

    public void setPassNewAction(int passNewAction)
    {
        this.passNewAction = passNewAction;
    }


    /**
     * Returns the pass message.
     * 
     * @return The pass message
     */

    public int getPassMessage()
    {
        return this.passMessage;
    }


    /**
     * Sets the pass message.
     * 
     * @param passMessage
     *            The pass message to set
     */

    public void setPassMessage(int passMessage)
    {
        this.passMessage = passMessage;
    }


    /**
     * Returns the unknown08.
     * 
     * @return The unknown08
     */

    public int getUnknown08()
    {
        return this.unknown08;
    }


    /**
     * Sets the unknown08.
     * 
     * @param unknown08
     *            The unknown08 to set
     */

    public void setUnknown08(int unknown08)
    {
        this.unknown08 = unknown08;
    }


    /**
     * Returns the unknown09.
     * 
     * @return The unknown09
     */

    public int getUnknown09()
    {
        return this.unknown09;
    }


    /**
     * Sets the unknown09.
     * 
     * @param unknown09
     *            The unknown09 to set
     */

    public void setUnknown09(int unknown09)
    {
        this.unknown09 = unknown09;
    }


    /**
     * Adds a new check
     * 
     * @param check
     *            The check to add
     */

    public void addCheck(Check check)
    {
        this.checks.add(check);
    }


    /**
     * Returns the check with the specified index.
     * 
     * @param index
     *            The index
     * @return The check
     */

    public Check getCheck(int index)
    {
        return this.checks.get(index);
    }


    /**
     * Sets a check at a specific index.
     * 
     * @param index
     *            The index
     * @param check
     *            The check
     */

    public void setCheck(int index, Check check)
    {
        this.checks.set(index, check);
    }


    /**
     * Deletes all checks
     */

    public void clearChecks()
    {
        this.checks.clear();
    }


    /**
     * Removes the check at the specified index.
     * 
     * @param index
     *            The index
     */

    public void removeCheck(int index)
    {
        this.checks.remove(index);
    }


    /**
     * Returns the number of checks.
     * 
     * @return The number of checks
     */

    public int countChecks()
    {
        return this.checks.size();
    }


    /**
     * Inserts a check at a specific index.
     * 
     * @param index
     *            The index
     * @param check
     *            The check
     */

    public void addCheck(int index, Check check)
    {
        this.checks.add(index, check);
    }
}
