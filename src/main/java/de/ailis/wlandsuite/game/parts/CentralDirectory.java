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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ailis.wlandsuite.game.blocks.GameMap;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * The central directory contains all the offsets to the various parts of the
 * map.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CentralDirectory
{
    /** The strings offset */
    private int stringsOffset;

    /** The monster names offset */
    private int monsterNamesOffset;

    /** The monster data offset */
    private int monsterDataOffset;

    /** The action class master table */
    private int[] actionClassMasterTable = new int[16];

    /** The nibble 6 offset */
    private int nibble6Offset;

    /** The NPC offset */
    private int npcOffset;


    /**
     * Constructor
     */

    public CentralDirectory()
    {
        super();
    }


    /**
     * Creates and returns a new Central Directory by reading it from the
     * specified stream.
     * 
     * @param stream
     *            The stream to read the central directory from.
     * @return The central directory
     * @throws IOException
     */

    public static CentralDirectory read(SeekableInputStream stream)
        throws IOException
    {
        CentralDirectory directory;

        // Create new Central Directory
        directory = new CentralDirectory();
        directory.stringsOffset = stream.readWord();
        directory.monsterNamesOffset = stream.readWord();
        directory.monsterDataOffset = stream.readWord();
        for (int i = 0; i < 16; i++)
        {
            directory.actionClassMasterTable[i] = stream.readWord();
        }
        directory.nibble6Offset = stream.readWord();
        directory.npcOffset = stream.readWord();
        if (stream.readWord() != 0)
        {
            throw new GameException("The unknown offset is not zero");
        }

        // Return the newly created Central Directory
        return directory;
    }


    /**
     * Writes the central directory to the specified output stream.
     * 
     * @param stream
     *            The stream to write the central directory to
     */

    public void write(SeekableOutputStream stream)
    {
        stream.writeWord(this.stringsOffset);
        stream.writeWord(this.monsterNamesOffset);
        stream.writeWord(this.monsterDataOffset);
        for (int offset: this.actionClassMasterTable)
        {
            stream.writeWord(offset);
        }
        stream.writeWord(this.nibble6Offset);
        stream.writeWord(this.npcOffset);
        stream.writeWord(0);
    }


    /**
     * Returns the monster data offset.
     * 
     * @return The monster data offset
     */

    public int getMonsterDataOffset()
    {
        return this.monsterDataOffset;
    }


    /**
     * Sets the monster data offset.
     * 
     * @param monsterDataOffset
     *            The monster data offset to set
     */

    public void setMonsterDataOffset(int monsterDataOffset)
    {
        this.monsterDataOffset = monsterDataOffset;
    }


    /**
     * Returns the monster names offset.
     * 
     * @return The monster names offset
     */

    public int getMonsterNamesOffset()
    {
        return this.monsterNamesOffset;
    }


    /**
     * Sets the monster names offset.
     * 
     * @param monsterNamesOffset
     *            The monster names offset to set
     */

    public void setMonsterNamesOffset(int monsterNamesOffset)
    {
        this.monsterNamesOffset = monsterNamesOffset;
    }


    /**
     * Returns the strings offset.
     * 
     * @return The strings offset
     */

    public int getStringsOffset()
    {
        return this.stringsOffset;
    }


    /**
     * Sets the strings offset.
     * 
     * @param stringsOffset
     *            The strings offset to set
     */

    public void setStringsOffset(int stringsOffset)
    {
        this.stringsOffset = stringsOffset;
    }

    /**
     * Returns the action class offset.
     * 
     * @param actionClass
     *            The action class
     * @return The offset
     */

    public int getActionClassOffset(int actionClass)
    {
        return this.actionClassMasterTable[actionClass];
    }


    /**
     * Sets an action class offset.
     * 
     * @param actionClass
     *            The action class
     * @param offset
     *            The offset
     */

    public void setActionClassOffset(byte actionClass, int offset)
    {
        this.actionClassMasterTable[actionClass] = offset;
    }


    /**
     * Returns the nibble6Offset.
     * 
     * @return The nibble6Offset
     */

    public int getNibble6Offset()
    {
        return this.nibble6Offset;
    }


    /**
     * Sets the nibble6Offset.
     * 
     * @param nibble6Offset
     *            The nibble6Offset to set
     */

    public void setNibble6Offset(int nibble6Offset)
    {
        this.nibble6Offset = nibble6Offset;
    }


    /**
     * Returns the npcOffset.
     * 
     * @return The npcOffset
     */

    public int getNpcOffset()
    {
        return this.npcOffset;
    }


    /**
     * Sets the npcOffset.
     * 
     * @param npcOffset
     *            The npcOffset to set
     */

    public void setNpcOffset(int npcOffset)
    {
        this.npcOffset = npcOffset;
    }


    /**
     * Sanitizes the central directory. This means zeroing all offsets which are
     * not used or set incorrectly.
     * 
     * @param gameMap
     *            The game map where the central directory is located in
     */

    public void sanitizeCentralDirectory(GameMap gameMap)
    {
        // Reset unused npc offset
        if ((this.npcOffset != 0) && (gameMap.getNpcs().size() == 0))
        {
            this.npcOffset = 0;
        }

        // Reset unused monster names offset
        if ((this.monsterNamesOffset != 0)
            && (gameMap.getMonsters().size() == 0))
        {
            this.monsterNamesOffset = 0;
        }

        // Reset unused monster names offset
        if ((this.monsterDataOffset != 0)
            && (gameMap.getMonsters().size() == 0))
        {
            this.monsterDataOffset = 0;
        }

        // Reset action class 15 if to encounters are on the map
        if ((this.actionClassMasterTable[15] != 0)
            && (gameMap.getInfo().getMaxEncounters() == 0))
        {
            this.actionClassMasterTable[15] = 0;
        }

        // Reset nibble6 offsets pointing to monster names
        if (this.nibble6Offset == this.monsterNamesOffset)
        {
            this.nibble6Offset = 0;
        }

        // Reset action classes which point to monster names
        for (int i = 0; i < 16; i++)
        {
            if (this.actionClassMasterTable[i] != 0
                && this.actionClassMasterTable[i] == this.monsterNamesOffset)
            {
                this.actionClassMasterTable[i] = 0;
            }
        }

        // Build a map from offsets to action classes
        Map<Integer, List<Integer>> usage = new HashMap<Integer, List<Integer>>();
        for (int i = 0; i < 16; i++)
        {
            List<Integer> classes;

            int offset = this.actionClassMasterTable[i];
            if (offset == 0) continue;

            classes = usage.get(offset);
            if (classes == null)
            {
                classes = new ArrayList<Integer>();
                usage.put(offset, classes);
            }
            classes.add(i);
        }

        // Cycle through the map and try to find and resolve conflicts
        for (Integer offset: usage.keySet())
        {
            List<Integer> classes = usage.get(offset);

            // No conflict? Then continue
            if (classes.size() < 2) continue;

            // Find out which class is really in use
            int usedClass = -1;
            for (Integer c: classes)
            {
                boolean used;

                used = gameMap.getActionMap().hasActionClass(c);
                if (used)
                {
                    if (usedClass >= 0)
                    {
                        throw new GameException("Offset " + offset
                            + " is in use by two action classes: " + usedClass
                            + " and " + c + ". This is not supported");
                    }
                    usedClass = c;
                }
            }

            // If action class 15 is involved then the other one
            // is the false one because we already tested that
            // action class 15 must be present
            if (usedClass == -1 && classes.contains(15))
            {
                usedClass = 15;
            }

            // Ugly hack to solve conflict in game1 map 10
            if (usedClass == -1 && classes.contains(6) && classes.contains(5)
                && offset == 6362)
            {
                usedClass = 6;
            }

            // Ugly hack to solve conflict in game1 map 12
            if (usedClass == -1 && classes.contains(6) && classes.contains(8)
                && classes.contains(7) && offset == 2376)
            {
                usedClass = 8;
            }

            // Ugly hack to solve conflict in game2 map 1
            if (usedClass == -1 && classes.contains(6) && classes.contains(12) && offset == 2064)
            {
                usedClass = 6;
            }

            // Ugly hack to solve conflict in game2 map 3
            if (usedClass == -1 && classes.contains(3) && classes.contains(4) && offset == 3197)
            {
                usedClass = 4;
            }

            // Ugly hack to solve conflict in game2 map 4
            if (usedClass == -1 && classes.contains(9) && classes.contains(10) && offset == 2178)
            {
                usedClass = 10;
            }

            // Ugly hack to solve conflict in game2 map 6
            if (usedClass == -1 && classes.contains(6) && classes.contains(8) && offset == 2038)
            {
                usedClass = 8;
            }

            // Ugly hack to solve conflict in game2 map 9
            if (usedClass == -1 && classes.contains(5) && classes.contains(6) && offset == 3051)
            {
                usedClass = 6;
            }

            // Ugly hack to solve conflict in game2 map 13
            if (usedClass == -1 && classes.contains(8) && classes.contains(9) && offset == 2622)
            {
                usedClass = 8;
            }

            // Only one class is used, reset all the others
            if (usedClass >= 0)
            {
                // Reset all other classes
                for (Integer c: classes)
                {
                    if (c.intValue() != usedClass)
                    {
                        this.actionClassMasterTable[c] = 0;
                    }
                }
                continue;
            }

            // Shit, don't know how to resolve it. Bail out.
            throw new GameException(
                "Don't know how to resolve conflicting action classes "
                    + classes);
        }
        
        // Reset nibble6 offset if action class 6 is not used
        if (this.nibble6Offset != 0 && this.actionClassMasterTable[6] == 0)
        {
            this.nibble6Offset = 0;
        }
    }
}
