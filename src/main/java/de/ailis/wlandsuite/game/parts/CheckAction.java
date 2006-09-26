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
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

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
    /** The logger */
    private static final Log log = LogFactory.getLog(CheckAction.class);
    
    /** If the square is passable even without a successful check */
    private boolean passable;

    /** If auto check should be used */
    private boolean autoCheck;

    /** If all party members are checked or only the first viable character */
    private boolean party;

    /** If damage should be applied to all members when check fails */
    private boolean damageAll;

    /** If all members must pass the check to succeed */
    private boolean passAll;

    /** Unknown bit in check */
    private boolean unknown1;

    /** Bypass armor when calculating damage */
    private boolean bypassArmor;

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

    /** If the modifier is a fixed value instead of a random dice role */
    private boolean fixedModifier;
    
    /** The modifier target */
    private int modifierTarget;
    
    /** The modifier */
    private int modifier;

    /** The checks */
    private List<Check> checks = new ArrayList<Check>();


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
        int flags, b;
        boolean checkBased;

        action = new CheckAction();

        flags = stream.readByte();
        action.passable = (flags & 128) == 128;
        action.autoCheck = (flags & 64) == 64;
        action.party = (flags & 32) == 32;
        action.damageAll = (flags & 16) == 16;
        checkBased = (flags & 8) == 8;
        action.passAll = (flags & 4) == 4;
        action.unknown1 = (flags & 2) == 2;
        action.bypassArmor = (flags & 1) == 1;
        action.startMessage = stream.readByte();
        action.passMessage = stream.readByte();
        action.failMessage = stream.readByte();
        action.passNewActionClass = stream.readByte();
        action.passNewAction = stream.readByte();
        action.failNewActionClass = stream.readByte();
        action.failNewAction = stream.readByte();
        b = stream.readByte();
        action.fixedModifier = (b & 128) == 128;
        action.modifierTarget = b & 127;
        b = stream.readByte();
        action.modifier = (b & 127) * (((b & 128) == 128) ? -1 : 1);

        while ((check = Check.read(stream)) != null)
        {
            action.checks.add(check);
        }

        // Read replacement data if present
        if (checkBased)
        {
            for (Check c: action.checks)
            {
                c.readReplacement(stream);
            }
        }

        // Bugfix for Safe-check on map 3 of game1
        if (flags == 64 && action.startMessage == 31
            && action.passMessage == 32 && action.failMessage == 0
            && action.passNewActionClass == 2 && action.passNewAction == 35
            && action.failNewActionClass == 2 && action.failNewAction == 35
            && action.modifierTarget == 0 && action.modifier == 0
            && !action.fixedModifier
            && action.checks.size() > 2
            && action.checks.get(2).getType() == Check.TYPE_UNKNOWN6)
        {
            log.info("Patching safe-check (5) on map 3");
            action.checks.remove(3);
            action.checks.remove(2);
        }

        // Bugfix for Safe-check on map 4 of game1
        if (flags == 228 && action.startMessage == 65
            && action.passMessage == 99 && action.failMessage == 0
            && action.passNewActionClass == 8 && action.passNewAction == 5
            && action.failNewActionClass == 255 && action.failNewAction == 0
            && action.modifierTarget == 0 && action.modifier == 4
            && !action.fixedModifier
            && action.checks.size() > 2
            && action.checks.get(2).getType() == Check.TYPE_UNKNOWN7)
        {
            log.info("Patching safe-check (20) on map 4");
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
        if (flags == 0 && action.startMessage == 36 && action.passMessage == 0
            && action.failMessage == 0 && action.passNewActionClass == 255
            && action.passNewAction == 255 && action.failNewActionClass == 255
            && action.failNewAction == 255 
            && action.modifierTarget == 0 && action.modifier == 0
            && !action.fixedModifier
            && action.checks.size() > 2
            && action.checks.get(2).getType() == Check.TYPE_UNKNOWN4)
        {
            log.info("Patching barrier-check (15) on map 4");
            action.checks.clear();
            action.checks.add(new Check());
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

        action.passable = Boolean.parseBoolean(element.attributeValue(
            "passable", "false"));
        action.autoCheck = Boolean.parseBoolean(element.attributeValue(
            "autoCheck", "false"));
        action.party = Boolean.parseBoolean(element.attributeValue("party",
            "false"));
        action.damageAll = Boolean.parseBoolean(element.attributeValue(
            "damageAll", "false"));
        action.passAll = Boolean.parseBoolean(element.attributeValue("passAll",
            "false"));
        action.unknown1 = Boolean.parseBoolean(element.attributeValue(
            "unknown1", "false"));
        action.bypassArmor = Boolean.parseBoolean(element.attributeValue(
            "bypassArmor", "false"));
        action.startMessage = StringUtils.toInt(element.attributeValue(
            "startMessage", "0"));
        action.passMessage = StringUtils.toInt(element.attributeValue(
            "passMessage", "0"));
        action.failMessage = StringUtils.toInt(element.attributeValue(
            "failMessage", "0"));
        action.passNewActionClass = StringUtils.toInt(element.attributeValue(
            "passNewActionClass", "255"));
        action.passNewAction = StringUtils.toInt(element.attributeValue(
            "passNewAction", "255"));
        action.failNewActionClass = StringUtils.toInt(element.attributeValue(
            "failNewActionClass", "255"));
        action.failNewAction = StringUtils.toInt(element.attributeValue(
            "failNewAction", "255"));
        action.fixedModifier = Boolean.parseBoolean(element.attributeValue("fixedModifier",
            "false"));
        action.modifierTarget = StringUtils.toInt(element.attributeValue(
            "modifierTarget", "0x1d"));
        action.modifier = StringUtils.toInt(element.attributeValue(
            "modifier", "0"));

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
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;

        element = XmlUtils.createElement("check");
        element.addAttribute("id", StringUtils.toHex(id));
        if (this.passable) element.addAttribute("passable", "true");
        if (this.autoCheck) element.addAttribute("autoCheck", "true");
        if (this.party) element.addAttribute("party", "true");
        if (this.damageAll) element.addAttribute("damageAll", "true");
        if (this.passAll) element.addAttribute("passAll", "true");
        if (this.unknown1) element.addAttribute("unknown1", "true");
        if (this.bypassArmor) element.addAttribute("bypassArmor", "true");
        if (this.startMessage != 0)
        {
            element.addAttribute("startMessage", Integer
                .toString(this.startMessage));
        }
        if (this.passMessage != 0)
        {
            element.addAttribute("passMessage", Integer
                .toString(this.passMessage));
        }
        if (this.failMessage != 0)
        {
            element.addAttribute("failMessage", Integer
                .toString(this.failMessage));
        }
        if (this.passNewActionClass != 255)
        {
            element.addAttribute("passNewActionClass", StringUtils.toHex
                (this.passNewActionClass));
        }
        if (this.passNewAction != 255)
        {
            element.addAttribute("passNewAction", StringUtils.toHex
                (this.passNewAction));
        }
        if (this.failNewActionClass != 255)
        {
            element.addAttribute("failNewActionClass", StringUtils.toHex
                (this.failNewActionClass));
        }
        if (this.failNewAction != 255)
        {
            element.addAttribute("failNewAction", StringUtils.toHex
                (this.failNewAction));
        }
        if (this.fixedModifier) element.addAttribute("fixedModifier", "true");
        if (this.modifierTarget != 0x1d)
        {
            element.addAttribute("modifierTarget", StringUtils.toHex(this.modifierTarget));
        }
        if (this.modifier != 0)
        {
            element.addAttribute("modifier", Integer.toString(this.modifier));
        }
        for (Check check: this.checks)
        {
            element.add(check.toXml());
        }

        return element;
    }


    /**
     * @throws IOException
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        int b;
        boolean checkBased;
        
        checkBased = false;
        for (Check check: this.checks)
        {
            if (check.getNewActionClass() != -1)
            {
                checkBased = true;
                break;
            }
        }

        b = this.passable ? 128 : 0;
        b |= this.autoCheck ? 64 : 0;
        b |= this.party ? 32 : 0;
        b |= this.damageAll ? 16 : 0;
        b |= checkBased ? 8 : 0;
        b |= this.passAll ? 4 : 0;
        b |= this.unknown1 ? 2 : 0;
        b |= this.bypassArmor ? 1 : 0;
        stream.write(b);
        stream.write(this.startMessage);
        stream.write(this.passMessage);
        stream.write(this.failMessage);
        stream.write(this.passNewActionClass);
        stream.write(this.passNewAction);
        stream.write(this.failNewActionClass);
        stream.write(this.failNewAction);
        b = this.fixedModifier ? 128 : 0;
        stream.write(b | (this.modifierTarget & 127));
        b = this.modifier < 0 ? 128 : 0;
        stream.write(b | (Math.abs(this.modifier)));

        for (Check check: this.checks)
        {
            check.write(stream);
        }
        stream.write(255);

        if (checkBased)
        {
            for (Check check: this.checks)
            {
                check.writeReplacement(stream);
            }
        }
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

    /**
     * Returns the autoCheck.
     * 
     * @return The autoCheck
     */

    public boolean isAutoCheck()
    {
        return this.autoCheck;
    }

    /**
     * Sets the autoCheck.
     * 
     * @param autoCheck
     *            The autoCheck to set
     */

    public void setAutoCheck(boolean autoCheck)
    {
        this.autoCheck = autoCheck;
    }

    /**
     * Returns the bypassArmor.
     * 
     * @return The bypassArmor
     */

    public boolean isBypassArmor()
    {
        return this.bypassArmor;
    }

    /**
     * Sets the bypassArmor.
     * 
     * @param bypassArmor
     *            The bypassArmor to set
     */

    public void setBypassArmor(boolean bypassArmor)
    {
        this.bypassArmor = bypassArmor;
    }

    /**
     * Returns the checks.
     * 
     * @return The checks
     */

    public List<Check> getChecks()
    {
        return this.checks;
    }

    /**
     * Sets the checks.
     * 
     * @param checks
     *            The checks to set
     */

    public void setChecks(List<Check> checks)
    {
        this.checks = checks;
    }

    /**
     * Returns the damageAll.
     * 
     * @return The damageAll
     */

    public boolean isDamageAll()
    {
        return this.damageAll;
    }

    /**
     * Sets the damageAll.
     * 
     * @param damageAll
     *            The damageAll to set
     */

    public void setDamageAll(boolean damageAll)
    {
        this.damageAll = damageAll;
    }

    /**
     * Returns the party.
     * 
     * @return The party
     */

    public boolean isParty()
    {
        return this.party;
    }

    /**
     * Sets the party.
     * 
     * @param party
     *            The party to set
     */

    public void setParty(boolean party)
    {
        this.party = party;
    }

    /**
     * Returns the passable.
     * 
     * @return The passable
     */

    public boolean isPassable()
    {
        return this.passable;
    }

    /**
     * Sets the passable.
     * 
     * @param passable
     *            The passable to set
     */

    public void setPassable(boolean passable)
    {
        this.passable = passable;
    }

    /**
     * Returns the passAll.
     * 
     * @return The passAll
     */

    public boolean isPassAll()
    {
        return this.passAll;
    }

    /**
     * Sets the passAll.
     * 
     * @param passAll
     *            The passAll to set
     */

    public void setPassAll(boolean passAll)
    {
        this.passAll = passAll;
    }

    
    /**
     * Returns the unknown1.
     * 
     * @return The unknown1
     */

    public boolean isUnknown1()
    {
        return this.unknown1;
    }

    
    /**
     * Sets the unknown1.
     * 
     * @param unknown1
     *            The unknown1 to set
     */

    public void setUnknown1(boolean unknown1)
    {
        this.unknown1 = unknown1;
    }

    
    /**
     * Returns the fixedModifier.
     *
     * @return The fixedModifier
     */
    
    public boolean isFixedModifier()
    {
        return this.fixedModifier;
    }

    
    /**
     * Sets the fixedModifier.
     *
     * @param fixedModifier 
     *            The fixedModifier to set
     */
    
    public void setFixedModifier(boolean fixedModifier)
    {
        this.fixedModifier = fixedModifier;
    }

    
    /**
     * Returns the modifier.
     *
     * @return The modifier
     */
    
    public int getModifier()
    {
        return this.modifier;
    }

    
    /**
     * Sets the modifier.
     *
     * @param modifier 
     *            The modifier to set
     */
    
    public void setModifier(int modifier)
    {
        this.modifier = modifier;
    }

    
    /**
     * Returns the modifierTarget.
     *
     * @return The modifierTarget
     */
    
    public int getModifierTarget()
    {
        return this.modifierTarget;
    }

    
    /**
     * Sets the modifierTarget.
     *
     * @param modifierTarget 
     *            The modifierTarget to set
     */
    
    public void setModifierTarget(int modifierTarget)
    {
        this.modifierTarget = modifierTarget;
    }
}
