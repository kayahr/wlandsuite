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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.ailis.wlandsuite.game.blocks.MapBlock;
import de.ailis.wlandsuite.game.blocks.SavegameBlock;
import de.ailis.wlandsuite.game.blocks.ShopItemsBlock;
import de.ailis.wlandsuite.game.parts.ActionClassMapPart;
import de.ailis.wlandsuite.game.parts.ActionSelectorMapPart;
import de.ailis.wlandsuite.game.parts.Part;
import de.ailis.wlandsuite.game.parts.UnknownPart;


/**
 * Game Block
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class GameBlock
{
    /** The block type */
    protected GameBlockType type;

    /** The list of block parts */
    protected SortedSet<Part> parts;


    /**
     * Empty Constructor
     */

    protected GameBlock()
    {
        this.parts = new TreeSet<Part>();
    }


    /**
     * Constructor
     * 
     * @param type
     *            The game block type
     */

    protected GameBlock(GameBlockType type)
    {
        this();
        this.type = type;
    }


    /**
     * Processes the children of an XML element
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    protected void processChildren(Element element)
    {
        int offset;
        String tagName;
        Part part;
        ActionClassMapPart actionClassMap = null;
        
        offset = 0;
        for (Element child: (List<Element>) element.elements())
        {
            tagName = child.getName();

            if (tagName.equals("unknown"))
            {
                part = new UnknownPart(offset, child);
            }
            else if (tagName.equals("actionClassMap"))
            {                
                part = new ActionClassMapPart(offset, child,
                    ((MapBlock) this).getMapSize());
                actionClassMap = (ActionClassMapPart) part;
            }
            else if (tagName.equals("actionSelectorMap"))
            {
                part = new ActionSelectorMapPart(offset, child,
                    ((MapBlock) this).getMapSize(), actionClassMap);
            }
            else
            {
                throw new GameException("Unknown game part type: " + tagName);
            }

            this.parts.add(part);
            offset += part.getSize();
        }
    }


    /**
     * Reads a game block from the specified input stream.
     * 
     * @param stream
     *            The input stream
     * @param disk
     *            The disk id
     * @param blockId
     *            The block id
     * @param size
     *            The block size
     * @return The game block
     * @throws IOException
     */

    public static GameBlock read(InputStream stream, int disk, int blockId,
        int size) throws IOException
    {
        RotatingXorInputStream gameStream;
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
            throw new IOException("Unable to read block " + blockId
                + " of game file " + disk + ". Need to read " + size
                + " bytes but only got " + read);
        }

        // Decrypt the whole block from the stream
        stream = new ByteArrayInputStream(rawBytes);
        gameStream = new RotatingXorInputStream(stream);
        size -= 2;
        bytes = new byte[size];
        gameStream.read(bytes);

        // Determine the block type
        type = getType(bytes);

        // And if the block is a map then we determine the size of the
        // encrypted block and read the data again
        if (type == GameBlockType.map)
        {
            encSize = MapBlock.getEncSize(bytes);
        }
        else if (type == GameBlockType.savegame)
        {
            encSize = SavegameBlock.getEncSize();
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
        checksum = gameStream.getChecksum();
        endChecksum = gameStream.getEndChecksum();
        if (endChecksum != checksum)
        {
            throw new IOException("Checksum error! Expected " + endChecksum
                + " but got " + checksum);
        }

        return createGameBlock(bytes, type);
    }


    /**
     * Creates a new game block.
     * 
     * @param bytes
     *            The block data
     * @return The game block
     */

    public static GameBlock createGameBlock(byte[] bytes)
    {
        return createGameBlock(bytes, getType(bytes));
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
        if (type == GameBlockType.map)
        {
            return new MapBlock(bytes);
        }
        else if (type == GameBlockType.savegame)
        {
            return new SavegameBlock(bytes);
        }
        else
        {
            return new ShopItemsBlock(bytes);
        }
    }


    /**
     * Writes the game block to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public abstract void write(OutputStream stream) throws IOException;


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
            return GameBlockType.savegame;
        }
        else if (isShopItems(bytes))
        {
            return GameBlockType.shopItems;
        }
        else
        {
            return GameBlockType.map;
        }
    }


    /**
     * Creates unknown parts to cover all the bytes which are not covered by
     * known parts.
     * 
     * @param bytes
     *            The bytes of the block
     */

    protected void createUnknownParts(byte[] bytes)
    {
        int start, end;

        start = 0;
        for (Part part: this.parts)
        {
            end = part.getOffset();
            if (start != end)
            {
                this.parts.add(new UnknownPart(bytes, start, end - start));
            }
            start = end + part.getSize();
        }
        end = bytes.length;
        this.parts.add(new UnknownPart(bytes, start, end - start));
    }


    /**
     * Converts the block into XML
     * 
     * @return The block as XML code
     */

    public abstract Element toXml();


    /**
     * Writes the block to a stream as XML
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void writeXml(OutputStream stream) throws IOException
    {
        XMLWriter writer;
        Document document;
        OutputFormat format;

        format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);

        writer = new XMLWriter(stream, format);
        try
        {
            document = DocumentHelper.createDocument(toXml());
            writer.write(document);
        }
        finally
        {
            writer.close();
        }
    }


    /**
     * Parses a gameblock from an XML stream.
     * 
     * @param stream
     *            The XML stream
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
                return new MapBlock(element);
            }
            else if (tagName.equals("savegame"))
            {
                return new SavegameBlock(element);
            }
            else if (tagName.equals("shopitems"))
            {
                return new ShopItemsBlock(element);
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
}
