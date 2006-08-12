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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameBlock;
import de.ailis.wlandsuite.game.GameBlockType;
import de.ailis.wlandsuite.game.GameException;
import de.ailis.wlandsuite.game.RotatingXorOutputStream;
import de.ailis.wlandsuite.game.parts.ActionClassMapPart;
import de.ailis.wlandsuite.game.parts.ActionSelectorMapPart;
import de.ailis.wlandsuite.game.parts.Part;


/**
 * Map block
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MapBlock extends GameBlock
{
    /** The map size. Only when block type is "map" */
    protected int mapSize;


    /**
     * Constructor
     */

    private MapBlock()
    {
        super(GameBlockType.map);
    }


    /**
     * Builds a map block from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public MapBlock(Element element)
    {
        this();

        this.mapSize = Integer.parseInt(element.attributeValue("size", "32"));
        processChildren(element);
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The block data
     */

    public MapBlock(byte[] bytes)
    {
        this(bytes, getMapSize(bytes));
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The block data
     * @param mapSize
     *            The map size
     */

    public MapBlock(byte[] bytes, int mapSize)
    {
        this();

        this.mapSize = mapSize;
        parse(bytes);
    }


    /**
     * Parses the game block
     * 
     * @param bytes
     *            The bytes of the game block to parse
     */

    private void parse(byte[] bytes)
    {
        ActionClassMapPart actionClassMap;

        // Create the action class map part
        actionClassMap = new ActionClassMapPart(bytes, this.mapSize);
        this.parts.add(actionClassMap);

        // Create the action selector map part
        this.parts.add(new ActionSelectorMapPart(bytes, this.mapSize,
            actionClassMap));

        createUnknownParts(bytes);
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
     * Writes the game block to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    @Override
    public void write(OutputStream stream) throws IOException
    {
        OutputStream gameStream;
        byte[] bytes;
        int encSize;
        int mapSize;
        int offset;
        ByteArrayOutputStream byteStream;

        // Create the byte array
        byteStream = new ByteArrayOutputStream();
        for (Part part: this.parts)
        {
            part.write(byteStream);
        }
        bytes = byteStream.toByteArray();

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
     * @see de.ailis.wlandsuite.game.GameBlock#toXml()
     */

    @Override
    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement("map");
        element.addAttribute("size", Integer.toString(this.mapSize));
        for (Part part: this.parts)
        {
            element.add(part.toXml());
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
}
