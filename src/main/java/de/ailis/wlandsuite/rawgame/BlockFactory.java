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

package de.ailis.wlandsuite.rawgame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.ailis.wlandsuite.rawgame.blocks.GameBlock;
import de.ailis.wlandsuite.rawgame.blocks.GameMap;
import de.ailis.wlandsuite.rawgame.blocks.Savegame;
import de.ailis.wlandsuite.rawgame.blocks.ShopItems;


/**
 * The Block Factory creates game blocks.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class BlockFactory
{
    /**
     * Private constructor to prevent instantiation
     */

    private BlockFactory()
    {
        super();
    }


    /**
     * Reads a game block from the specified input stream. The block size must
     * be specified because it cannot be read from the input stream.
     * 
     * @param stream
     *            The input stream
     * @param size
     *            The block size
     * @param decrypt
     *            If data should be decrypted
     * @return The game block
     * @throws IOException
     */

    public static GameBlock read(InputStream stream, int size, boolean decrypt)
        throws IOException
    {
        InputStream gameStream;
        byte[] rawBytes;
        byte[] bytes;
        int read;
        int checksum, endChecksum;
        GameBlockType type;
        int encSize;

        // Read the raw block
        rawBytes = new byte[size];
        read = stream.read(rawBytes);
        if (read != size)
        {
            throw new IOException("Unable to read game block. Need to read "
                + size + " bytes but only got " + read);
        }

        // Decrypt the whole block from the stream
        stream = new ByteArrayInputStream(rawBytes);
        if (decrypt)
        {
            gameStream = new RotatingXorInputStream(stream);
        }
        else
        {
            gameStream = stream;
            
            // Read the dummy checksum
            gameStream.skip(2);
        }
        size -= 2;
        bytes = new byte[size];
        gameStream.read(bytes);
        
        // Determine the block type
        type = getType(bytes);

        if (decrypt)
        {
            // And if the block is a map then we determine the size of the
            // encrypted block and read the data again
            if (type == GameBlockType.MAP)
            {
                encSize = GameMap.getEncSize(bytes);
            }
            else if (type == GameBlockType.SAVEGAME)
            {
                encSize = Savegame.getEncSize();
            }
            else
            {
                encSize = bytes.length;
            }
    
            // Decrypt data again if not all data is encrypted
            if (encSize < bytes.length)
            {
                stream = new ByteArrayInputStream(rawBytes);
    
                // Read the encrypted part of the data
                gameStream = new RotatingXorInputStream(stream);
                gameStream.read(bytes, 0, encSize);
    
                // Read the unecrypted part of the data
                stream.read(bytes, encSize, bytes.length - encSize);
            }

            // Check the checksum
            checksum = ((RotatingXorInputStream) gameStream).getChecksum();
            endChecksum = ((RotatingXorInputStream) gameStream).getEndChecksum();
            if (endChecksum != checksum)
            {
                throw new IOException("Checksum error! Expected " + endChecksum
                    + " but got " + checksum);
            }
        }

        // Return and create the game block
        return createGameBlock(bytes, type);
    }


    /**
     * Creates a new game block.
     * 
     * @param bytes
     *            The block data
     * @param type
     *            The block type
     * @return The game block
     */

    private static GameBlock createGameBlock(byte[] bytes, GameBlockType type)
    {
        if (type == GameBlockType.MAP)
        {
            return new GameMap(bytes);
        }
        else if (type == GameBlockType.SAVEGAME)
        {
            return new Savegame(bytes);
        }
        else
        {
            return new ShopItems(bytes);
        }
    }


    /**
     * Reads a game block from the specified XML stream
     * 
     * @param stream
     *            The input stream
     * @return The game block
     * @throws IOException
     */

    public static GameBlock readXml(InputStream stream) throws IOException
    {
        SAXReader reader;
        Document document;
        Element element;
        String tagName;

        reader = new SAXReader();
        try
        {
            document = reader.read(stream);
            element = document.getRootElement();
            tagName = element.getName();
            if (tagName.equals("map"))
            {
                return new GameMap(element);
            }
            else if (tagName.equals("savegame"))
            {
                return new Savegame(element);
            }
            else if (tagName.equals("shopitems"))
            {
                return new ShopItems(element);
            }
            else
            {
                throw new GameException("Unknown game block type: " + tagName);
            }
        }
        catch (DocumentException e)
        {
            throw new IOException("Unable to parse XML gam block: "
                + e.getMessage());
        }
    }


    /**
     * Returns the game block type of the specified block data.
     * 
     * @param bytes
     *            The block data
     * @return The game block type
     */

    private static GameBlockType getType(byte[] bytes)
    {
        if (isSaveGame(bytes))
        {
            return GameBlockType.SAVEGAME;
        }
        else if (isShopItems(bytes))
        {
            return GameBlockType.SHOPITEMS;
        }
        else
        {
            return GameBlockType.MAP;
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
        if (bytes.length == 4608)
        {
            for (int i = 1; i < 8; i++)
            {
                b = bytes[i];
                if (b > 7) return false;
                if (b != 0 && seen.contains(Integer.valueOf(b))) return false;
                seen.add(Integer.valueOf(b));
            }
            return true;
        }
        return false;
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
        if (bytes.length == 760)
        {
            if (bytes[0] == 0x60 && bytes[1] == 0x60 && bytes[2] == 0x60)
            {
                return true;
            }
            return false;
        }
        return false;
    }
}
