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

package de.ailis.wlandsuite.rawgame.parts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.ailis.wlandsuite.utils.XmlUtils;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.huffman.HuffmanOutputStream;
import de.ailis.wlandsuite.huffman.HuffmanTree;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Tiles Map.
 * 
 * TODO The tiles map location is currently searched by a time consuming
 * algorithm. This is bad and should be fixed by finding the information in the
 * file where the tiles map begins.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class TilesMap extends AbstractPart
{
    /** The tiles map. First index is y, second is x */
    private int[][] map;

    /** A unknown 32 bit address */
    private int unknown;


    /**
     * Creates a tiles map part from XML.
     * 
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     */

    public TilesMap(Element element, int mapSize)
    {
        this(mapSize);

        String data;
        String c;
        int i;
        int b;

        this.unknown = Integer.parseInt(element.attributeValue("unknown"));
        this.offset = Integer.parseInt(element.attributeValue("offset"));
        this.size = Integer.parseInt(element.attributeValue("size"));
        data = element.getTextTrim();
        i = 0;
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                try
                {
                    c = data.substring(i * 3, i * 3 + 2);
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    throw new GameException("Tiles map is corrupt: "
                        + (mapSize * mapSize - (y * mapSize + x))
                        + " bytes missing");
                }
                if (c.equals("..")) c = "00";
                try
                {
                    b = Integer.valueOf(c, 16);
                }
                catch (NumberFormatException e)
                {
                    throw new GameException("Illegal data in tiles map at y="
                        + y + " x=" + x);
                }
                this.map[y][x] = b;
                i++;
            }
        }
    }


    /**
     * Constructor
     * 
     * @param mapSize
     *            The map size
     */

    public TilesMap(int mapSize)
    {
        this.map = new int[mapSize][mapSize];
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The bytes of the game block
     * @param mapSize
     *            The map size
     */

    public TilesMap(byte[] bytes, int mapSize)
    {
        this(mapSize);

        int i = bytes.length - 12;
        while (i > 0)
        {
            if ((bytes[i] == 0) && (bytes[i + 1] == ((mapSize * mapSize) >> 8))
                && (bytes[i + 2] == 0) && (bytes[i + 3] == 0)
                && (bytes[i + 6] == 0) && (bytes[i + 7] == 0))
            {
                try
                {
                    HuffmanInputStream stream = new HuffmanInputStream(
                        new ByteArrayInputStream(bytes, i + 8, bytes.length
                            - (i + 8)));
                    for (int y = 0; y < mapSize; y++)
                    {
                        for (int x = 0; x < mapSize; x++)
                        {
                            this.map[y][x] = stream.read();
                        }
                    }
                    if (stream.available() == 0)
                    {
                        this.offset = i;
                        this.unknown = ((bytes[i + 4] & 0xff) << 0)
                            | ((bytes[i + 5] & 0xff) << 8)
                            | ((bytes[i + 6] & 0xff) << 16)
                            | ((bytes[i + 7] & 0xff) << 24);
                        break;
                    }
                }
                catch (IOException e)
                {
                    // Ignored
                }
            }
            i--;
        }
        /*if (i == 0)
        {
            throw new GameException("No tilesMap found in map");
        }*/
        this.size = bytes.length - this.offset;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;
        StringWriter text;
        PrintWriter writer;

        element = XmlUtils.createElement("tilesMap");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("size", Integer.toString(this.size));
        element.addAttribute("unknown", Integer.toString(this.unknown));

        text = new StringWriter();
        writer = new PrintWriter(text);

        writer.println();
        for (int y = 0, yMax = this.map.length; y < yMax; y++)
        {
            writer.print("    ");
            for (int x = 0, xMax = this.map[y].length; x < xMax; x++)
            {
                int b;

                if (x > 0)
                {
                    writer.print(" ");
                }

                b = this.map[y][x];
                writer.format("%02x", new Object[] { b });
            }
            writer.println();
        }
        writer.print("  ");

        element.add(DocumentHelper.createText(text.toString()));
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        ByteArrayOutputStream byteStream;
        BitOutputStreamWrapper bitStream;
        byte[] bytes;

        byteStream = new ByteArrayOutputStream();
        bitStream = new BitOutputStreamWrapper(stream);
/*
        for (int y = 0, yMax = this.map.length; y < yMax; y++)
        {
            for (int x = 0, xMax = this.map[y].length; x < xMax; x++)
            {
                byteStream.write(this.map[y][x]);
            }
        }
        bytes = byteStream.toByteArray();
        bitStream.write(bytes);
        if (1==1) return;
*/
        // Write padding bytes
        int padding = this.offset - offset;
        if (padding < 0)
        {
            throw new GameException("Too much data before tiles map");
        }
        for (int i = 0; i < padding; i++)
        {
            bitStream.write(0);
        }

        bitStream.writeInt(this.map.length * this.map.length);
        bitStream.writeInt(this.unknown);
        for (int y = 0, yMax = this.map.length; y < yMax; y++)
        {
            for (int x = 0, xMax = this.map[y].length; x < xMax; x++)
            {
                byteStream.write(this.map[y][x]);
            }
        }
        bytes = byteStream.toByteArray();

        byteStream = new ByteArrayOutputStream();
        HuffmanTree tree = HuffmanTree.create(bytes);
        HuffmanOutputStream hstream = new HuffmanOutputStream(byteStream, tree);
        hstream.write(bytes);
        hstream.flush();
        bytes = byteStream.toByteArray();
        
        // Add padding bytes and complain if new compressed data is too large 
        padding = this.size - 8 - bytes.length;
        if (padding < 0)
        {
            throw new GameException("Tile map is " + (-padding) + " bytes too large");
        }
        stream.write(bytes);
        for (int i = 0; i < padding; i++)
        {
            stream.write(0);
        }
    }


    /**
     * Returns the tile for the specified position
     * 
     * @param x
     *            The X position
     * @param y
     *            The Y position
     * @return The action selector
     */

    public int getTile(int x, int y)
    {
        return this.map[y][x];
    }


    /**
     * Returns the unknown 32 bit address.
     *
     * @return The unknown 32 bit address
     */
    
    public int getUnknown()
    {
        return this.unknown;
    }
}
