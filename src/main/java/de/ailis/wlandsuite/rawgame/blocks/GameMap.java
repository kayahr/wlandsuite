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

package de.ailis.wlandsuite.rawgame.blocks;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.rawgame.GameBlockType;
import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.rawgame.RotatingXorOutputStream;
import de.ailis.wlandsuite.rawgame.parts.ActionClassMap;
import de.ailis.wlandsuite.rawgame.parts.ActionSelectorMap;
import de.ailis.wlandsuite.rawgame.parts.AlterCode;
import de.ailis.wlandsuite.rawgame.parts.Alteration;
import de.ailis.wlandsuite.rawgame.parts.CentralDirectory;
import de.ailis.wlandsuite.rawgame.parts.CheckCode;
import de.ailis.wlandsuite.rawgame.parts.CodePointerTable;
import de.ailis.wlandsuite.rawgame.parts.ImpassableCode;
import de.ailis.wlandsuite.rawgame.parts.MapInfo;
import de.ailis.wlandsuite.rawgame.parts.MaskCode;
import de.ailis.wlandsuite.rawgame.parts.MonsterData;
import de.ailis.wlandsuite.rawgame.parts.MonsterNames;
import de.ailis.wlandsuite.rawgame.parts.NPCList;
import de.ailis.wlandsuite.rawgame.parts.Part;
import de.ailis.wlandsuite.rawgame.parts.RadiationCode;
import de.ailis.wlandsuite.rawgame.parts.SimpleCode;
import de.ailis.wlandsuite.rawgame.parts.Strings;
import de.ailis.wlandsuite.rawgame.parts.TilesMap;
import de.ailis.wlandsuite.rawgame.parts.TransitionCode;
import de.ailis.wlandsuite.rawgame.parts.UnknownPart;


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

    /** The tiles map */
    private TilesMap tilesMap;

    /** The central directory */
    private CentralDirectory centralDirectory;

    /** The map info */
    private MapInfo mapInfo;

    /** The NPC list */
    private NPCList npcList;

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
            else if (tagName.equals("tilesMap"))
            {
                part = this.tilesMap = new TilesMap(child, this.mapSize);
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
            else if (tagName.equals("simple"))
            {
                part = new SimpleCode(child);
            }
            else if (tagName.equals("check"))
            {
                part = new CheckCode(child);
            }
            else if (tagName.equals("impassable"))
            {
                part = new ImpassableCode(child);
            }
            else if (tagName.equals("mask"))
            {
                part = new MaskCode(child);
            }
            else if (tagName.equals("alter"))
            {
                part = new AlterCode(child);
            }
            else if (tagName.equals("monsterNames"))
            {
                part = new MonsterNames(child);
            }
            else if (tagName.equals("monsterData"))
            {
                part = new MonsterData(child);
            }
            else if (tagName.equals("strings"))
            {
                part = new Strings(child);
            }
            else if (tagName.equals("mapInfo"))
            {
                part = new MapInfo(child);
            }
            else if (tagName.equals("npcList"))
            {
                part = new NPCList(child);
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

        // Read the tiles map
        this.tilesMap = new TilesMap(bytes, this.mapSize);
        this.parts.add(this.tilesMap);

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

        // Read map info
        this.mapInfo = new MapInfo(bytes, offset
            + this.centralDirectory.getSize());
        this.parts.add(this.mapInfo);

        // Read NPC list if present
        offset = this.centralDirectory.getNpcOffset();
        if (offset != 0)
        {
            this.npcList = new NPCList(bytes, offset);
            this.parts.add(this.npcList);
        }

        if (this.centralDirectory.getMonsterDataOffset() != 0)
        {
            int monsters = (this.centralDirectory.getStringsOffset() - this.centralDirectory
                .getMonsterDataOffset()) / 8;

            // Parse monster names
            this.parts.add(new MonsterNames(bytes, this.centralDirectory
                .getMonsterNamesOffset(), monsters));

            this.parts.add(new MonsterData(bytes, this.centralDirectory
                .getMonsterDataOffset(), monsters));
        }

        // Parse the strings
        this.parts.add(new Strings(bytes, this.centralDirectory
            .getStringsOffset(), this.tilesMap.getOffset()));

        // Cycle through all action class offsets and build code pointer tables
        Map<Integer, CodePointerTable> tables;
        tables = new HashMap<Integer, CodePointerTable>(16);
        this.codePointerTables = new HashMap<Integer, CodePointerTable>(16);
        for (int i = 0; i < 16; i++)
        {
            CodePointerTable codePointerTable;

            offset = this.centralDirectory.getActionClassOffset(i);
            if (offset == 0) continue;
            if (offset == this.centralDirectory.getMonsterNamesOffset())
                continue;

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
                parseCode(bytes, actionClass, actionSelector);
            }
        }

        createUnknownPartsForCodeStrings(bytes);

        // Create unknown parts for all the data left
        createUnknownParts(bytes);
    }


    /**
     * Parses an action code.
     * 
     * @param bytes
     *            The block data
     * @param actionClass
     *            The action class
     * @param actionSelector
     *            The action selector
     */

    private void parseCode(byte[] bytes, int actionClass, int actionSelector)
    {
        while (actionClass != 255 && actionClass != 0)
        {
            CodePointerTable table = this.codePointerTables.get(actionClass);
            if (table == null) break;
            int pointer = table.getCodePointer(actionSelector);
            if (hasPart(pointer)) return;
            switch (actionClass)
            {
                case 1:
                    SimpleCode simple = new SimpleCode(bytes, pointer);
                    this.parts.add(simple);
                    actionClass = simple.getActionClass();
                    actionSelector = simple.getActionSelector();
                    break;

                case 2:
                    CheckCode check = new CheckCode(bytes, pointer);
                    this.parts.add(check);
                    parseCode(bytes, check.getFailActionClass(), check
                        .getFailActionSelector());
                    actionClass = check.getPassActionClass();
                    actionSelector = check.getPassActionSelector();
                    break;

                case 4:
                    MaskCode mask = new MaskCode(bytes, pointer);
                    this.parts.add(mask);
                    actionClass = mask.getActionClass();
                    actionSelector = mask.getActionSelector();
                    break;

                case 9:
                    RadiationCode radiation = new RadiationCode(bytes, pointer);
                    this.parts.add(radiation);
                    actionClass = radiation.getActionClass();
                    actionSelector = radiation.getActionSelector();
                    break;

                case 10:
                    TransitionCode transition = new TransitionCode(bytes,
                        pointer);
                    this.parts.add(transition);
                    actionClass = transition.getActionClass();
                    actionSelector = transition.getActionSelector();
                    break;

                case 11:
                    ImpassableCode impassable = new ImpassableCode(bytes,
                        pointer);
                    this.parts.add(impassable);
                    actionClass = impassable.getActionClass();
                    actionSelector = impassable.getActionSelector();
                    break;

                case 12:
                    AlterCode alter = new AlterCode(bytes,
                        pointer);
                    this.parts.add(alter);
                    for (Alteration alteration: alter.getAlterations())
                    {
                        parseCode(bytes, alteration.getActionClass(), alteration
                            .getActionSelector());
                        
                    }
                    actionClass = alter.getActionClass();
                    actionSelector = alter.getActionSelector();
                    break;

                default:
                    actionClass = 255;
            }
        }
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
     * @see de.ailis.wlandsuite.rawgame.blocks.GameBlock#write(java.io.OutputStream,
     *      boolean)
     */

    public void write(OutputStream stream, boolean encrypt) throws IOException
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
        if (encrypt)
        {
            gameStream = new RotatingXorOutputStream(stream);
        }
        else
        {
            gameStream = stream;

            // Write dummy checksum to keep the filesize
            gameStream.write(0);
            gameStream.write(0);
        }
        gameStream.write(bytes, 0, encSize);
        gameStream.flush();

        // Write the unencrypted data
        stream.write(bytes, encSize, bytes.length - encSize);
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.blocks.GameBlock#toXml()
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
     * Determines the map size by just looking at the MSQ block bytes. For this
     * it needs at least 6189 unencrypted bytes. Throws a GameException if it
     * was not able to determine the map size.
     * 
     * @param bytes
     *            The MSQ block bytes
     * @return The map size.
     */

    private static int getMapSize(byte[] bytes)
    {
        int offset;
        boolean is32, is64;

        // Check if map can be size 64
        is64 = false;
        offset = 64 * 64 * 3 / 2;
        if (offset + 44 < bytes.length)
        {
            if (bytes[offset + 44] == 64 && bytes[offset + 6] == 0
                && bytes[offset + 7] == 0)
            {
                is64 = true;
            }
        }

        // Check if map can be size 3
        is32 = false;
        offset = 32 * 32 * 3 / 2;
        if (offset + 44 < bytes.length && bytes[offset + 6] == 0
            && bytes[offset + 7] == 0)
        {
            if (bytes[offset + 44] == 32)
            {
                is32 = true;
            }
        }

        // Complain if map can be both sizes
        if (!is32 && !is64)
        {
            throw new GameException(
                "Cannot determine map size: Map is not a 32 or 64 size map");
        }
        if (is32 && is64)
        {
            throw new GameException(
                "Cannot determine map size: Map could be a 32 or 64 size map");
        }

        return is32 ? 32 : 64;
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


    /**
     * Returns the tilesMap.
     * 
     * @return The tilesMap
     */

    public TilesMap getTilesMap()
    {
        return this.tilesMap;
    }


    /**
     * Returns the mapInfo.
     * 
     * @return The mapInfo
     */

    public MapInfo getMapInfo()
    {
        return this.mapInfo;
    }
}
