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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.game.RotatingXorInputStream;
import de.ailis.wlandsuite.game.RotatingXorOutputStream;
import de.ailis.wlandsuite.game.parts.ActionClassMap;
import de.ailis.wlandsuite.game.parts.ActionMap;
import de.ailis.wlandsuite.game.parts.Actions;
import de.ailis.wlandsuite.game.parts.BattleStrings;
import de.ailis.wlandsuite.game.parts.CentralDirectory;
import de.ailis.wlandsuite.game.parts.Info;
import de.ailis.wlandsuite.game.parts.Monsters;
import de.ailis.wlandsuite.game.parts.NPCs;
import de.ailis.wlandsuite.game.parts.SpecialAction;
import de.ailis.wlandsuite.game.parts.SpecialActionTable;
import de.ailis.wlandsuite.game.parts.Strings;
import de.ailis.wlandsuite.game.parts.TileMap;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * A game map.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class GameMap extends GameBlock implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = 3535759235422069966L;

    /** The logger */
    private static final Log log = LogFactory.getLog(GameMap.class);

    /** The map size */
    private int mapSize;

    /** The MSQ block size */
    private int msqSize;

    /** The offset of the tilemap */
    private int tilemapOffset;

    /** The map info */
    private Info info;

    /** The battle strings */
    private BattleStrings battleStrings;

    /** The action map */
    private ActionClassMap actionClassMap;

    /** The action map */
    private ActionMap actionMap;

    /** The tiles map */
    private TileMap tileMap;

    /** The strings */
    private Strings strings;

    /** The NPCs */
    private NPCs npcs;

    /** The monsters */
    private Monsters monsters;

    /** The actions */
    private Map<Integer, Actions> actions;


    /**
     * Constructs a new map with the specified map size. The map size must be 64
     * or 32. Maps are always quadratic. It's not possible to have different
     * widths and heights.
     * 
     * The MSQ block size must be specified so the object knows how many padding
     * bytes must be inserted during save. MSQ block sizes are hardcoded in the
     * EXE so new Maps must fit these hardcoded boundaries to be loadable.
     * 
     * The tilemap offset must also be specified because the position of the
     * tilemap is also hardcoded in the exe. So a new map must know where to
     * save the tilemap in the map file.
     * 
     * @param mapSize
     *            The map size
     * @param msqBlockSize
     *            The MSQ block size
     * @param tilemapOffset
     *            The offset of the tilemap
     */

    public GameMap(int mapSize, int msqBlockSize, int tilemapOffset)
    {
        if (mapSize != 32 && mapSize != 64)
        {
            throw new IllegalArgumentException("Illegal map size specified: "
                + mapSize + ". Valid sizes are 32 and 64");
        }
        this.mapSize = mapSize;
        this.msqSize = msqBlockSize;
        this.tilemapOffset = tilemapOffset;
        this.actionClassMap = new ActionClassMap(mapSize);
        this.actionMap = new ActionMap(mapSize);
        this.actions = new HashMap<Integer, Actions>(15);
    }


    /**
     * Constructs a map by reading it from a wasteland gameX file stream. The
     * stream must point at the beginning of the MSQ block (which is at the "m"
     * of the "msq" header string.
     * 
     * Because it's not possible to read the length of a MSQ block from the MSQ
     * block itself the size of the block must be specified manually.
     * 
     * @param stream
     *            The input stream
     * @param msqBlockSize
     *            The block size
     * @return The newly constructed Game Map
     * @throws IOException
     */

    public static GameMap read(SeekableInputStream stream, int msqBlockSize)
        throws IOException
    {
        byte[] headerBytes;
        String header;
        RotatingXorInputStream xorStream;
        byte[] bytes;
        int mapSize;
        int encSize;
        int tilemapOffset;
        GameMap gameMap;
        long startOffset;

        // Read the MSQ block header and validate it
        headerBytes = new byte[4];
        stream.read(headerBytes);
        header = new String(headerBytes, "ASCII");
        if (!header.equals("msq0") && !header.equals("msq1"))
        {
            throw new IOException("No MSQ block header found at stream");
        }

        // Get the starting offset
        startOffset = stream.tell();

        // Read/Decrypt beginning of the MSQ block body
        bytes = new byte[6189];
        xorStream = new RotatingXorInputStream(stream);
        xorStream.read(bytes);

        // Determine the map size and initialize the map with it
        mapSize = determineMapSize(bytes);

        // Determine the encryption size
        encSize = determineEncryptionSize(bytes, mapSize);

        // Read/Decrypt the whole block
        bytes = new byte[msqBlockSize - 6];
        stream.seek(startOffset);
        xorStream = new RotatingXorInputStream(stream);
        xorStream.read(bytes, 0, encSize);
        stream.read(bytes, encSize, bytes.length - encSize);

        // Determine the tiles offset
        tilemapOffset = determineTilesOffset(bytes, mapSize);

        // Create the byte array stream and begin parsing the input
        stream = new SeekableInputStream(new ByteArrayInputStream(bytes));

        // Create the Game Map
        gameMap = new GameMap(mapSize, msqBlockSize, tilemapOffset);

        // Read the map data
        gameMap.readMapData(stream, tilemapOffset, mapSize, true);

        // Return the created map
        return gameMap;
    }


    /**
     * Reads the map data from the given stream. This method is internally
     * called by the read and read and readHacked method.
     * 
     * @param stream
     *            The input stream
     * @param tilemapOffset
     *            The offset of the tilemap
     * @param mapSize
     *            The size of the map
     * @param compressedTileMap
     *            Defines if the tilemap is compressed and therefor must be
     *            decompressed first
     * @throws IOException
     */

    private void readMapData(SeekableInputStream stream, int tilemapOffset,
        int mapSize, boolean compressedTileMap) throws IOException
    {
        CentralDirectory centralDirectory;
        SpecialActionTable specialActionTable;
        int monsterDataOffset;

        // Read the action map
        this.actionClassMap = ActionClassMap.read(stream, mapSize);

        // Read the action map
        this.actionMap = ActionMap.read(stream, mapSize);

        // Read the central directory
        centralDirectory = CentralDirectory.read(stream);

        // Read the map info
        stream.skip(1);
        this.info = Info.read(stream);

        // Read the battle strings
        this.battleStrings = BattleStrings.read(stream);

        // Read the tiles map
        stream.seek(tilemapOffset);
        this.tileMap = TileMap.read(stream, compressedTileMap ? 0 : mapSize);

        // Read the strings
        stream.seek(centralDirectory.getStringsOffset());
        this.strings = Strings.read(stream, tilemapOffset);

        // Read the NPCs
        stream.seek(centralDirectory.getNpcOffset());
        this.npcs = NPCs.read(stream);

        // Read the monsters if present
        monsterDataOffset = centralDirectory.getMonsterDataOffset();
        if (monsterDataOffset != 0)
        {
            int quantity;

            quantity = (centralDirectory.getStringsOffset() - monsterDataOffset) / 8;

            stream.seek(centralDirectory.getMonsterNamesOffset());
            this.monsters = Monsters.read(stream, monsterDataOffset, quantity);
        }
        else
        {
            this.monsters = new Monsters();
        }

        // Sanitizes the central directory
        centralDirectory.sanitizeCentralDirectory(this);

        // Read the special action table
        stream.seek(centralDirectory.getNibble6Offset());
        specialActionTable = SpecialActionTable.read(stream, 128);

        // Read the actions
        for (int i = 1; i < 16; i++)
        {
            int offset = centralDirectory.getActionClassOffset(i);
            if (offset != 0)
            {
                stream.seek(offset);
                this.actions
                    .put(i, Actions.read(i, stream, specialActionTable));
            }
            else
            {
                this.actions.put(i, new Actions());
            }
        }
    }


    /**
     * Reads a hacked map (For Displacer's hacked EXE) from the given input
     * stream.
     * 
     * @param stream
     *            The input stream
     * @return The map
     * @throws IOException
     */

    public static GameMap readHacked(InputStream stream) throws IOException
    {
        int tilemapOffset, mapSize;
        SeekableInputStream gameStream;
        GameMap map;

        gameStream = new SeekableInputStream(stream);
        tilemapOffset = gameStream.readWord();
        mapSize = gameStream.read();
        if (tilemapOffset == -1 || mapSize == -1)
        {
            throw new IOException("Unexpected end of stream while reading map");
        }

        gameStream = new SeekableInputStream(stream);
        map = new GameMap(mapSize, 0, 0);
        map.readMapData(gameStream, tilemapOffset, mapSize, false);

        return map;
    }


    /**
     * Writes the map data to the specified output stream. This method is used
     * internally by the write and writeHacked methods.
     * 
     * @param stream
     *            The output stream
     * @param compressTilemap
     *            If the tile map should be compressed
     * @return The central directory
     * @throws IOException
     */

    private CentralDirectory writeMapData(OutputStream stream,
        boolean compressTilemap) throws IOException
    {
        SeekableOutputStream plainStream;
        CentralDirectory centralDirectory;
        int stringsOffset;
        long directoryOffset;
        SpecialActionTable specialActionTable;

        plainStream = new SeekableOutputStream(stream);

        // Write the action class map
        this.actionClassMap.write(plainStream);

        // Write the action map
        this.actionMap.write(plainStream);

        // Create the central directory and skip the space for it in the map
        centralDirectory = new CentralDirectory();
        directoryOffset = plainStream.tell();
        plainStream.skip(44);

        // Write the map size
        plainStream.writeByte(this.mapSize);

        // Write the map info
        this.info.write(plainStream);

        // Write the battle strings
        this.battleStrings.write(plainStream);

        // Build the special action table
        specialActionTable = buildSpecialActionTable();

        // Write the actions
        for (int i = 1; i < 16; i++)
        {
            Actions actions;

            actions = this.actions.get(i);
            if (actions == null || actions.countActions() == 0)
            {
                continue;
            }

            centralDirectory.setActionClassOffset(i, (int) plainStream.tell());
            actions.write(plainStream, specialActionTable);
        }

        // Write the special action table
        if (specialActionTable.size() > 0)
        {
            centralDirectory.setNibble6Offset((int) plainStream.tell());
            specialActionTable.write(plainStream);
        }

        // Write the NPCs
        if (this.npcs.size() > 0)
        {
            centralDirectory.setNpcOffset((int) plainStream.tell());
            this.npcs.write(plainStream);
        }

        // Write the monster names
        centralDirectory.setMonsterNamesOffset((int) plainStream.tell());
        this.monsters.writeNames(plainStream);

        // Write the monster data
        centralDirectory.setMonsterDataOffset((int) plainStream.tell());
        this.monsters.writeData(plainStream);

        // Write the strings
        stringsOffset = (int) plainStream.tell();
        centralDirectory.setStringsOffset(stringsOffset);
        this.strings.write(plainStream);

        // Add padding
        if (compressTilemap)
        {
            if (plainStream.tell() > this.tilemapOffset)
            {
                log.warn("Too much data before tile map. Fixing "
                    + "offsets in wl.exe is needed to run this game file");
            }
            else
            {
                plainStream.skip(this.tilemapOffset - plainStream.tell());
            }
        }

        // Write the tile map
        centralDirectory.setTilemapOffset((int) plainStream.tell());
        this.tileMap.write(plainStream, compressTilemap);

        // Add padding
        if (compressTilemap)
        {
            if (plainStream.tell() > this.msqSize - 6)
            {
                log.warn("Tilemap too large. Fixing offsets in wl.exe is needed "
                    + "to run this game file");
            }
            else
            {
                plainStream.skip(this.msqSize - 6 - plainStream.tell());
            }
        }

        // Write the central directory
        plainStream.seek(directoryOffset);
        centralDirectory.write(plainStream);

        // Flush the stream, it's complete now
        plainStream.flush();

        return centralDirectory;
    }

    /**
     * Writes the map to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param disk
     *            The disk id (0 or 1)
     * @throws IOException
     */

    public void write(OutputStream stream, int disk) throws IOException
    {
        ByteArrayOutputStream byteStream;
        RotatingXorOutputStream xorStream;
        byte[] bytes;
        CentralDirectory centralDirectory;
        int stringsOffset;

        byteStream = new ByteArrayOutputStream();
        centralDirectory = writeMapData(byteStream, true);
        bytes = byteStream.toByteArray();
        stringsOffset = centralDirectory.getStringsOffset();

        // Write the MSQ header
        stream.write("msq".getBytes());
        stream.write('0' + disk);

        // Write the encrypted data
        xorStream = new RotatingXorOutputStream(stream);
        xorStream.write(bytes, 0, stringsOffset);
        xorStream.flush();

        // Write the unencrypted data
        stream.write(bytes, stringsOffset, bytes.length - stringsOffset);
    }


    /**
     * Writes an external map file compatible to Displacer's hacked EXE file.
     * 
     * @param stream
     *            The stream to write the map to
     * @throws IOException
     */

    public void writeHacked(OutputStream stream) throws IOException
    {
        ByteArrayOutputStream byteStream;
        byte[] bytes;
        CentralDirectory centralDirectory;
        int tilemapOffset;

        byteStream = new ByteArrayOutputStream();
        centralDirectory = writeMapData(byteStream, false);
        tilemapOffset = centralDirectory.getTilemapOffset();
        bytes = byteStream.toByteArray();

        // Write the data
        stream.write(tilemapOffset & 255);
        stream.write(tilemapOffset >> 8);
        stream.write(this.mapSize);
        stream.write(bytes);
    }


    /**
     * Builds the special action table by looking at the actions in action class
     * 6.
     * 
     * @return The special action table
     */

    private SpecialActionTable buildSpecialActionTable()
    {
        Actions actions;
        SpecialActionTable specialActionTable;

        specialActionTable = new SpecialActionTable();

        actions = this.actions.get(6);
        if (actions != null)
        {
            for (int i = 0, max = actions.countActions(); i < max; i++)
            {
                try
                {
                    SpecialAction action = (SpecialAction) actions.getAction(i);
                    if (action != null)
                    {
                        int id = action.getAction();
                        if (!specialActionTable.contains(id))
                        {
                            specialActionTable.add(id);
                        }
                    }
                }
                catch (ClassCastException e)
                {
                    // Ignored
                }
            }
        }
        return specialActionTable;
    }


    /**
     * Creates and returns a new game map from XML.
     * 
     * @param element
     *            The XML root element
     * @return The Game Map
     */

    public static GameMap read(Element element)
    {
        GameMap gameMap;
        int mapSize;
        int msqSize;
        int tilemapOffset;

        // Read map configuration
        mapSize = StringUtils.toInt(element.attributeValue("mapSize"));
        msqSize = StringUtils.toInt(element.attributeValue("msqSize", "0"));
        tilemapOffset = StringUtils.toInt(element.attributeValue(
            "tilemapOffset", "0"));

        // Create the new map
        gameMap = new GameMap(mapSize, msqSize, tilemapOffset);

        // Parse the action map
        gameMap.actionClassMap = ActionClassMap.read(element
            .element("actionClassMap"), mapSize);

        // Parse the action map
        gameMap.actionMap = ActionMap.read(element.element("actionMap"),
            mapSize);

        // Parse the map info
        gameMap.info = Info.read(element.element("info"));

        // Parse the battle strings
        gameMap.battleStrings = BattleStrings.read(element
            .element("battleStrings"));

        // Read the actions
        for (Object item: element.elements("actions"))
        {
            Element subElement = (Element) item;
            int actionClass;

            actionClass = StringUtils.toInt(subElement
                .attributeValue("actionClass"));
            gameMap.actions.put(actionClass, Actions.read(subElement));
        }

        // Parse the tile map
        gameMap.tileMap = TileMap.read(element.element("tileMap"), mapSize,
            gameMap.info.getBackgroundTile());

        // Parse the strings
        gameMap.strings = Strings.read(element.element("strings"));

        // Parse the monsters
        gameMap.monsters = Monsters.read(element.element("monsters"));

        // Parse the NPCs
        gameMap.npcs = NPCs.read(element.element("npcs"));

        return gameMap;
    }


    /**
     * Reads a game map from the specified XML stream.
     * 
     * @param stream
     *            The input stream
     * @return The game map
     */

    public static GameMap readXml(InputStream stream)
    {
        Document document;
        Element element;

        document = XmlUtils.readDocument(stream);
        element = document.getRootElement();
        return read(element);
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#toXml()
     */

    @Override
    public Element toXml()
    {
        Element element;

        // Create the root element
        element = XmlUtils.createElement("map");
        element.addAttribute("mapSize", Integer.toString(this.mapSize));
        if (this.msqSize != 0)
        {
            element.addAttribute("msqSize", Integer.toString(this.msqSize));
        }
        if (this.tilemapOffset != 0)
        {
            element.addAttribute("tilemapOffset", Integer
                .toString(this.tilemapOffset));
        }

        // Add the action map
        element.add(this.actionClassMap.toXml());

        // Add the action map
        element.add(this.actionMap.toXml(this.actionClassMap));

        // Add the map info
        element.add(this.info.toXml());

        // Add the battle strings
        element.add(this.battleStrings.toXml());

        // Add the actions
        for (int i = 1; i < 16; i++)
        {
            Actions actions;

            actions = this.actions.get(i);
            if (actions != null && actions.countActions() > 0)
            {
                element.add(actions.toXml(i));
            }
        }

        // Add the NPCs
        element.add(this.npcs.toXml());

        // Add the monsters
        element.add(this.monsters.toXml());

        // Add the strings
        element.add(this.strings.toXml());

        // Add the tiles map
        element.add(this.tileMap.toXml(this.info.getBackgroundTile()));

        // Return the XMl element
        return element;
    }


    /**
     * Returns the size of the encrypted part in the map block. To do this it
     * needs at least 6146 decrypted bytes from the map block.
     * 
     * @param bytes
     *            The (decrypted) block data
     * @param mapSize
     *            The map size
     * @return The size of the encrypted part
     */

    private static int determineEncryptionSize(byte[] bytes, int mapSize)
    {
        int offset;

        offset = mapSize * mapSize * 3 / 2;
        return ((bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8));
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

    private static int determineMapSize(byte[] bytes)
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
     * Determines the tiles offset by just looking at the MSQ block bytes.
     * 
     * @param bytes
     *            The MSQ block bytes
     * @param mapSize
     *            The map size
     * @return The tiles offset
     */

    private static int determineTilesOffset(byte[] bytes, int mapSize)
    {
        int i = bytes.length - 9;
        while (i > 0)
        {
            if ((bytes[i] == 0) && (bytes[i + 1] == ((mapSize * mapSize) >> 8))
                && (bytes[i + 2] == 0) && (bytes[i + 3] == 0)
                && (bytes[i + 6] == 0) && (bytes[i + 7] == 0))
            {
                return i;
            }
            i--;
        }
        throw new GameException("Unable to find tiles offset for size "
            + mapSize + " map");
    }


    /**
     * Returns the map size. This is normally 64 or 32. Maps are always
     * quadratic. It's not possible to have different widths and heights.
     * 
     * @return The map size
     */

    public int getMapSize()
    {
        return this.mapSize;
    }


    /**
     * Returns the MSQ block size.
     * 
     * @return The MSQ block size
     */

    public int getMsqSize()
    {
        return this.msqSize;
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
        this.actionClassMap = actionClassMap;
    }


    /**
     * Returns the actionMap.
     * 
     * @return The actionMap
     */

    public ActionMap getActionMap()
    {
        return this.actionMap;
    }


    /**
     * Sets the actionMap.
     * 
     * @param actionMap
     *            The actionMap to set
     */

    public void setActionMap(ActionMap actionMap)
    {
        this.actionMap = actionMap;
    }


    /**
     * Returns the battleStrings.
     * 
     * @return The battleStrings
     */

    public BattleStrings getBattleStrings()
    {
        return this.battleStrings;
    }


    /**
     * Sets the battleStrings.
     * 
     * @param battleStrings
     *            The battleStrings to set
     */

    public void setBattleStrings(BattleStrings battleStrings)
    {
        this.battleStrings = battleStrings;
    }


    /**
     * Returns the info.
     * 
     * @return The info
     */

    public Info getInfo()
    {
        return this.info;
    }


    /**
     * Sets the info.
     * 
     * @param info
     *            The info to set
     */

    public void setInfo(Info info)
    {
        this.info = info;
    }


    /**
     * Returns the monsters.
     * 
     * @return The monsters
     */

    public Monsters getMonsters()
    {
        return this.monsters;
    }


    /**
     * Sets the monsters.
     * 
     * @param monsters
     *            The monsters to set
     */

    public void setMonsters(Monsters monsters)
    {
        this.monsters = monsters;
    }


    /**
     * Returns the npcs.
     * 
     * @return The npcs
     */

    public NPCs getNpcs()
    {
        return this.npcs;
    }


    /**
     * Sets the npcs.
     * 
     * @param npcs
     *            The npcs to set
     */

    public void setNpcs(NPCs npcs)
    {
        this.npcs = npcs;
    }


    /**
     * Returns the strings.
     * 
     * @return The strings
     */

    public Strings getStrings()
    {
        return this.strings;
    }


    /**
     * Sets the strings.
     * 
     * @param strings
     *            The strings to set
     */

    public void setStrings(Strings strings)
    {
        this.strings = strings;
    }


    /**
     * Returns the tileMap.
     * 
     * @return The tileMap
     */

    public TileMap getTileMap()
    {
        return this.tileMap;
    }


    /**
     * Sets the tileMap.
     * 
     * @param tileMap
     *            The tileMap to set
     */

    public void setTileMap(TileMap tileMap)
    {
        this.tileMap = tileMap;
    }


    /**
     * Returns the tilemapOffset.
     * 
     * @return The tilemapOffset
     */

    public int getTilemapOffset()
    {
        return this.tilemapOffset;
    }


    /**
     * Sets the tilemapOffset.
     * 
     * @param tilemapOffset
     *            The tilemapOffset to set
     */

    public void setTilemapOffset(int tilemapOffset)
    {
        this.tilemapOffset = tilemapOffset;
    }


    /**
     * Sets the mapSize.
     * 
     * @param mapSize
     *            The mapSize to set
     */

    public void setMapSize(int mapSize)
    {
        this.mapSize = mapSize;
    }


    /**
     * Sets the msqSize.
     * 
     * @param msqSize
     *            The msqSize to set
     */

    public void setMsqSize(int msqSize)
    {
        this.msqSize = msqSize;
    }
}
