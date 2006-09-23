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

package de.ailis.wlandsuite.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ailis.wlandsuite.game.blocks.GameMap;
import de.ailis.wlandsuite.game.blocks.Savegame;
import de.ailis.wlandsuite.game.blocks.ShopItemList;
import de.ailis.wlandsuite.io.SeekableInputStream;


/**
 * Represents a game file with all its maps, the savegame and shop lists.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Game
{
    /** The logger */
    private static final Log log = LogFactory.getLog(Game.class);
    
    /** The map block type */
    private static final int TYPE_MAP = 0;

    /** The savegame block type */
    private static final int TYPE_SAVEGAME = 1;

    /** The shop list block type */
    private static final int TYPE_SHOPLIST = 2;

    /** The game maps */
    private List<GameMap> maps;

    /** The save game */
    private Savegame savegame;

    /** The shop item lists */
    private List<ShopItemList> shopItemLists;


    /**
     * Constructor
     */

    public Game()
    {
        this.maps = new ArrayList<GameMap>(21);
        this.shopItemLists = new ArrayList<ShopItemList>();
    }


    /**
     * Creates and returns a new Game object by reading it from the specified
     * input stream.
     * 
     * @param stream
     *            The input stream to read the game file from
     * @return The newly created Game object
     * @throws IOException
     */

    public static Game read(InputStream stream) throws IOException
    {
        Game game;
        SeekableInputStream gameStream;
        int mapNo;
        int listNo;

        // Construct a new Game object
        game = new Game();

        // Wrap the input stream with a seekable input stream for easier access
        gameStream = new SeekableInputStream(stream);

        // Cycle over all msq blocks
        mapNo = 0;
        listNo = 0;
        for (GameMsqBlock block: getMsqBlocks(gameStream))
        {
            int type;

            gameStream.seek(block.getOffset());
            type = getType(gameStream, block.getSize());
            gameStream.seek(block.getOffset());

            switch (type)
            {
                case TYPE_MAP:
                    log.info("Reading map " + mapNo);
                    mapNo++;
                    game.maps.add(GameMap.read(gameStream, block.getSize()));
                    break;

                case TYPE_SAVEGAME:
                    log.info("Reading savegame");
                    game.savegame = Savegame.read(gameStream);
                    break;

                case TYPE_SHOPLIST:
                    log.info("Reading shop item list " + listNo);
                    listNo++;
                    game.shopItemLists.add(ShopItemList.read(gameStream));
                    break;
            }
        }

        // Read the whole game file into
        return game;
    }


    /**
     * Writes the game file to the specified output stream. The disk number
     * is determined automatically by looking at the number of maps.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        write(stream, this.maps.size() == 20 ? 0 : 1);
    }
    
    
    /**
     * Writes the game file to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param disk
     *            The disk id (0 or 1)
     * @throws IOException
     */

    public void write(OutputStream stream, int disk) throws IOException
    {
        int i;
        
        // Write the maps
        i = 0;
        for (GameMap map: this.maps)
        {
            log.info("Writing map " + i);
            map.write(stream, disk);
            i++;
        }

        // Write the savegame
        log.info("Writing savegame");
        this.savegame.write(stream, disk);

        // Write the shop item lists
        i = 0;
        for (ShopItemList list: this.shopItemLists)
        {
            log.info("Writing shop item list " + i);
            list.write(stream, disk);
            i++;
        }
    }


    /**
     * Returns the maps of the game file.
     * 
     * @return The maps
     */

    public GameMap[] getMaps()
    {
        return this.maps.toArray(new GameMap[0]);
    }


    /**
     * Returns the shop item lists of the game file.
     * 
     * @return The shop item lists
     */

    public ShopItemList[] getShopItemLists()
    {
        return this.shopItemLists.toArray(new ShopItemList[0]);
    }


    /**
     * Returns the game structure. The structure consists of a list of
     * GameMsqBlock objects which describes the offset and size of each MSQ
     * block. The offsets and sizes are determined by searching for the msq[0-1]
     * header. This will break if such a text appears in the middle of a MSQ
     * block. But because this is quite a uncommon string and because of the
     * encryption and compression this will most likely not happen.
     * 
     * @param stream
     *            The stream to read the game data from
     * @return The game file structure
     * @throws IOException
     *             When the game file could not be read
     */

    private static List<GameMsqBlock> getMsqBlocks(InputStream stream)
        throws IOException
    {
        List<GameMsqBlock> blocks;
        int nextOffset;
        int offset;
        int disk;
        int stage;
        int b;
        byte[] tmp;
        String header;

        blocks = new ArrayList<GameMsqBlock>(23);

        // Read the first header to validate the file and get the disk
        // number
        offset = 0;
        tmp = new byte[4];
        stream.read(tmp);
        header = new String(tmp);
        if (!header.equals("msq0") && !header.equals("msq1"))
        {
            throw new IOException("No msq header found in stream");
        }
        disk = tmp[3];
        nextOffset = offset;
        offset = 4;

        // Read the rest of the file and scan for msq blocks.
        stage = 0;
        while ((b = stream.read()) != -1)
        {
            switch (stage)
            {
                case 0:
                    if (b == 'm')
                    {
                        stage = 1;
                    }
                    break;

                case 1:
                    if (b == 's')
                    {
                        stage = 2;
                    }
                    else
                    {
                        stage = 0;
                    }
                    break;

                case 2:
                    if (b == 'q')
                    {
                        stage = 3;
                    }
                    else
                    {
                        stage = 0;
                    }
                    break;

                case 3:
                    if (b == disk)
                    {
                        blocks.add(new GameMsqBlock(nextOffset, offset - 3
                            - nextOffset));
                        nextOffset = offset - 3;
                    }
                    stage = 0;
                    break;
            }
            offset++;
        }

        blocks.add(new GameMsqBlock(nextOffset, offset - nextOffset));
        return blocks;
    }


    /**
     * Returns the game block type of the MSQ block the specified stream points
     * to. This method reads the first 9 decrypted bytes from the block. You
     * have to reset the stream if you want to read the MSQ block from it after
     * this.
     * 
     * @param stream
     *            The stream pointing to a MSQ block
     * @param msqBlockSize
     *            The MSQ block size
     * @return The game block type
     * @throws IOException
     *             If stream cannot be read or doesn't contain a valid MSQ block
     */

    private static int getType(InputStream stream, int msqBlockSize)
        throws IOException
    {
        byte[] bytes;
        InputStream xorStream;
        String header;

        // Validate header
        bytes = new byte[4];
        stream.read(bytes);
        header = new String(bytes);
        if (!header.equals("msq0") && !header.equals("msq1"))
        {
            throw new IOException("No msq header found in stream");
        }

        // Read the first 9 decrypted bytes
        xorStream = new RotatingXorInputStream(stream);
        bytes = new byte[9];
        xorStream.read(bytes);

        // Determine and return the type
        if (msqBlockSize == 4614 && isSaveGame(bytes))
        {
            return TYPE_SAVEGAME;
        }
        else if (msqBlockSize == 766 && isShopItems(bytes))
        {
            return TYPE_SHOPLIST;
        }
        else
        {
            return TYPE_MAP;
        }
    }


    /**
     * Checks if the specified byte array represents a Wasteland save game. Save
     * games are discovered by the block size and by the byte offsets 1-8 which
     * represents the character order and must contain values between 0 and 7
     * while all non-zero numbers can only occur once.
     * 
     * @param bytes
     *            The byte array to check
     * @return If it's a save game or not
     */

    private static boolean isSaveGame(byte[] bytes)
    {
        List<Integer> seen;
        byte b;

        seen = new ArrayList<Integer>(7);
        for (int i = 1; i < 8; i++)
        {
            b = bytes[i];
            if (b > 7) return false;
            if (b != 0 && seen.contains(Integer.valueOf(b))) return false;
            seen.add(Integer.valueOf(b));
        }
        return true;
    }


    /**
     * Checks if the specified byte array represents an unknown block which is
     * one of the blocks following the save game block. Purpose of these blocks
     * is currently unknown.
     * 
     * @param bytes
     *            The byte array to check
     * @return If it's an unknwon block or not
     */

    private static boolean isShopItems(byte[] bytes)
    {
        if (bytes[0] == 0x60 && bytes[1] == 0x60 && bytes[2] == 0x60)
        {
            return true;
        }
        return false;
    }


    /**
     * Adds a new map to the game.
     * 
     * @param map
     *            The map to add
     */

    public void addMap(GameMap map)
    {
        this.maps.add(map);
    }


    /**
     * Adds a new shop item list to the game.
     * 
     * @param shopItemList
     *            The shop item list to add
     */

    public void addShopItemList(ShopItemList shopItemList)
    {
        this.shopItemLists.add(shopItemList);
    }


    /**
     * Returns the savegame.
     * 
     * @return The savegame
     */

    public Savegame getSavegame()
    {
        return this.savegame;
    }


    /**
     * Sets the savegame.
     * 
     * @param savegame
     *            The savegame to set
     */

    public void setSavegame(Savegame savegame)
    {
        this.savegame = savegame;
    }
}
