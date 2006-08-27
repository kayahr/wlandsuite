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

package de.ailis.wlandsuite.game.blocks;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameBlockType;
import de.ailis.wlandsuite.game.GameException;
import de.ailis.wlandsuite.game.RotatingXorOutputStream;
import de.ailis.wlandsuite.game.parts.ActionClassMap;
import de.ailis.wlandsuite.game.parts.ActionSelectorMap;
import de.ailis.wlandsuite.game.parts.CentralDirectory;
import de.ailis.wlandsuite.game.parts.CodePointerTable;
import de.ailis.wlandsuite.game.parts.Part;
import de.ailis.wlandsuite.game.parts.RadiationCode;
import de.ailis.wlandsuite.game.parts.TransitionCode;
import de.ailis.wlandsuite.game.parts.UnknownPart;


/**
 * Map block
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class GameMap extends AbstractGameBlock
{
    /** The map size. Only when block type is "map" */
    private int mapSize;

    /** The action class map */
    private ActionClassMap actionClassMap;

    /** The action selector map */
    private ActionSelectorMap actionSelectorMap;

    /** The central directory */
    private CentralDirectory centralDirectory;

    /** The action class code pointer tables */
    private Map<Integer, CodePointerTable> codePointerTables;


    /**
     * Constructor
     */

    private GameMap()
    {
        super(GameBlockType.MAP);
        this.codePointerTables = new HashMap<Integer, CodePointerTable>();
    }


    /**
     * Builds a map block from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public GameMap(Element element)
    {
        this();

        String tagName;
        Part part;

        this.mapSize = Integer.parseInt(element.attributeValue("size", "32"));

        for (Element child: (List<Element>) element.elements())
        {
            tagName = child.getName();

            if (tagName.equals("unknown"))
            {
                part = new UnknownPart(child);
            }
            else if (tagName.equals("actionClassMap"))
            {
                part = this.actionClassMap = new ActionClassMap(child,
                    this.mapSize);
            }
            else if (tagName.equals("actionSelectorMap"))
            {
                part = this.actionSelectorMap = new ActionSelectorMap(child,
                    this.mapSize, this.actionClassMap);
            }
            else if (tagName.equals("centralDirectory"))
            {
                part = this.centralDirectory = new CentralDirectory(child);
            }
            else if (tagName.equals("codePointers"))
            {
                CodePointerTable table = new CodePointerTable(child);
                part = table;
                for (Integer actionClass: table.getActionClasses())
                {
                    this.codePointerTables.put(actionClass, table);
                }
            }
            else if (tagName.equals("transition"))
            {
                part = new TransitionCode(child);
            }
            else if (tagName.equals("radiation"))
            {
                part = new RadiationCode(child);
            }
            else
            {
                throw new GameException("Unknown game part type: " + tagName);
            }

            this.parts.add(part);
        }
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The block data
     */

    public GameMap(byte[] bytes)
    {
        this(bytes, getMapSize(bytes));
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The complete block data. Map is parsed from it
     * @param mapSize
     *            The map size
     */

    public GameMap(byte[] bytes, int mapSize)
    {
        this();

        int offset;

        // Remember the map size
        this.mapSize = mapSize;

        // Parse the action class map part
        this.actionClassMap = new ActionClassMap(bytes, this.mapSize);
        this.parts.add(this.actionClassMap);

        // Parse the action selector map part
        this.actionSelectorMap = new ActionSelectorMap(bytes, this.mapSize,
            this.actionClassMap);
        this.parts.add(this.actionSelectorMap);

        // Parse the central directory
        offset = mapSize * mapSize * 3 / 2;
        this.centralDirectory = new CentralDirectory(bytes, offset);
        this.parts.add(this.centralDirectory);

        // Cycle through all action class offsets and build code pointer tables
        Map<Integer, CodePointerTable> tables;
        tables = new HashMap<Integer, CodePointerTable>(16);
        this.codePointerTables = new HashMap<Integer, CodePointerTable>(16);
        for (int i = 0; i < 16; i++)
        {
            CodePointerTable codePointerTable;

            offset = this.centralDirectory.getActionClassOffset(i);
            if (offset == 0) continue;

            // Create the code pointer table
            codePointerTable = tables.get(offset);
            if (codePointerTable == null)
            {
                codePointerTable = new CodePointerTable(bytes, offset, i);
                tables.put(offset, codePointerTable);
                this.parts.add(codePointerTable);
            }
            else
            {
                codePointerTable.addActionClass(i);
            }
            this.codePointerTables.put(i, codePointerTable);
        }

        // Parse transition codes
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                int actionClass = this.actionClassMap.getActionClass(x, y);
                if (actionClass == 0) continue;
                int actionSelector = this.actionSelectorMap.getActionSelector(
                    x, y);
                int pointer = this.codePointerTables.get(actionClass)
                    .getCodePointer(actionSelector);
                if (hasPart(pointer)) continue;
                switch (actionClass)
                {
                    case 10:
                        this.parts.add(new TransitionCode(bytes, pointer));
                        break;

                    case 9:
                        this.parts.add(new RadiationCode(bytes, pointer));
                        break;
                }
            }
        }

        createUnknownPartsForCodeStrings(bytes);

        // Create unknown parts for all the data left
        createUnknownParts(bytes);
    }
    

    /**
     * Checks if there is already a part with the specified offset.
     * 
     * @param offset
     *            The offset
     * @return If there is alreary a part at this offset or not
     */

    private boolean hasPart(int offset)
    {
        for (Part part: this.parts)
        {
            if (part.getOffset() == offset)
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Create unknown parts for code strings.
     * 
     * @param bytes
     *            The game block bytes
     */

    private void createUnknownPartsForCodeStrings(byte[] bytes)
    {
        int lastOffset;

        for (CodePointerTable table: this.codePointerTables.values())
        {
            lastOffset = -1;
            for (int i = 0; i < table.getCodePointers(); i++)
            {
                int pointer = table.getCodePointer(i);
                if (pointer == 0) continue;

                if (lastOffset >= 0)
                {
                    if (!hasPart(lastOffset))
                    {
                        if (lastOffset < bytes.length && pointer < bytes.length
                            && pointer > lastOffset)
                        {
                            this.parts.add(new UnknownPart(bytes, lastOffset,
                                pointer - lastOffset));
                        }
                    }
                }
                lastOffset = pointer;
            }
        }
    }


    /**
     * Returns the size of the encrypted part in the map block
     * 
     * @param bytes
     *            The (fully decrypted) block data
     * @return The size of the encrypted part
     */

    public static int getEncSize(byte[] bytes)
    {
        int mapSize;
        int offset;

        mapSize = getMapSize(bytes);
        offset = mapSize * mapSize * 3 / 2;
        return ((bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8));
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#write(java.io.OutputStream)
     */

    public void write(OutputStream stream) throws IOException
    {
        OutputStream gameStream;
        byte[] bytes;
        int encSize;
        int mapSize;
        int offset;

        // Create the game block data
        bytes = createBlockData();

        // Only the stuff before the strings is encrypted. So we get
        // the string offset and use it as the size.
        mapSize = this.mapSize;
        offset = mapSize * mapSize * 3 / 2;
        encSize = ((bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8));

        // Write the encrypted data
        gameStream = new RotatingXorOutputStream(stream);
        gameStream.write(bytes, 0, encSize);
        gameStream.flush();

        // Write the unencrypted data
        stream.write(bytes, encSize, bytes.length - encSize);
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#toXml()
     */

    public Element toXml()
    {
        Element element, partElement;

        element = DocumentHelper.createElement("map");
        element.addAttribute("size", Integer.toString(this.mapSize));
        for (Part part: this.parts)
        {
            partElement = part.toXml();
            if (partElement != null)
            {
                element.add(part.toXml());
            }
        }
        return element;
    }


    /**
     * Returns the map size. This method does not look for the map size in the
     * EXE file. Instead it tries to "guess" it by looking at some
     * characteristics of the byte array. This is not totaly safe but it works
     * fine.
     * 
     * If the map size could not be determined then a GameException is thrown.
     * 
     * @param bytes
     *            The bytes of the map block
     * @return The map size
     * @throws GameException
     *             If size could not be determined
     */

    private static int getMapSize(byte[] bytes) throws GameException
    {
        int start;
        int offset;

        // Cycle over possible map sizes
        size: for (int size = 32; size <= 64; size *= 2)
        {
            // Calculate start of central directory
            start = size * size * 3 / 2;

            // Read 19 offsets of the central directory and validate them
            for (int i = 0; i < 19; i++)
            {
                // Read offset
                try
                {
                    offset = (bytes[start + i * 2] & 0xff)
                        | ((bytes[start + i * 2 + 1] & 0xff) << 8);
                }
                catch (IndexOutOfBoundsException e)
                {
                    // Out of bounds? Size must be wrong
                    continue size;
                }

                // Validate offset
                if (offset != 0 && (offset < start || offset > bytes.length))
                {
                    continue size;
                }
            }

            // Everything looks fine. This size is correct
            return size;
        }

        // Found no valid size? Strange. Throw exception
        throw new GameException("Unable to determine map size");
    }


    /**
     * Returns the map size.
     * 
     * @return The map size
     */

    public int getMapSize()
    {
        return this.mapSize;
    }

    /**
     * Sets the map size.
     * 
     * @param mapSize
     *            The map size to set
     */

    public void setMapSize(int mapSize)
    {
        this.mapSize = mapSize;
    }


    /**
     * Returns the action class map.
     * 
     * @return The action class map
     */

    public ActionClassMap getActionClassMap()
    {
        return this.actionClassMap;
    }


    /**
     * Sets the action class map.
     * 
     * @param actionClassMap
     *            The action class map to set
     */

    public void setActionClassMap(ActionClassMap actionClassMap)
    {
        if (this.actionClassMap != null)
        {
            this.parts.remove(this.actionClassMap);
        }
        this.actionClassMap = actionClassMap;
        this.parts.add(actionClassMap);
    }


    /**
     * Returns the action selector map.
     * 
     * @return The action selector map
     */

    public ActionSelectorMap getActionSelectorMap()
    {
        return this.actionSelectorMap;
    }


    /**
     * Sets the action selector map.
     * 
     * @param actionSelectorMap
     *            The action selector map to set
     */

    public void setActionSelectorMap(ActionSelectorMap actionSelectorMap)
    {
        if (this.actionSelectorMap != null)
        {
            this.parts.remove(this.actionSelectorMap);
        }
        this.actionSelectorMap = actionSelectorMap;
        this.parts.add(actionSelectorMap);
    }
}
