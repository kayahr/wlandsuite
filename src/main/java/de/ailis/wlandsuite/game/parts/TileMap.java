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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.huffman.HuffmanOutputStream;
import de.ailis.wlandsuite.huffman.HuffmanTree;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The Tile Map maps eachs square to a specific tile of the selected tileset.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class TileMap
{
    /** The tile map. First index is y, second is x */
    private final int[][] map;

    /** A unknown 32 bit address */
    private int unknown;


    /**
     * Constructs a new Tile Map with the specified map size. Map size must be
     * 32 or 64. If an other size is specified then an IllegalArgumentException
     * is thrown
     *
     * @param mapSize
     *            The map size (32 or 64)
     * @throws IllegalArgumentException
     *             If illegal map size was specified
     */

    public TileMap(final int mapSize)
    {
        if (mapSize != 32 && mapSize != 64)
        {
            throw new IllegalArgumentException("Illegal map size specified: "
                + mapSize);
        }
        this.map = new int[mapSize][mapSize];
    }


    /**
     * Creates and returns a new Tile Map from XML.
     *
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     * @param backgroundTile
     *            The background tile which is used for ".." bytes
     * @return The new Tile Map
     */

    public static TileMap read(final Element element, final int mapSize,
        final int backgroundTile)
    {
        TileMap tileMap;
        String data;
        String c;
        int i;
        int b;

        // Create the new Tile Map
        tileMap = new TileMap(mapSize);

        tileMap.unknown = StringUtils.toInt(element.attributeValue("unknown",
            "0"));
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
                catch (final StringIndexOutOfBoundsException e)
                {
                    throw new GameException("Tile map is corrupt: "
                        + (mapSize * mapSize - (y * mapSize + x))
                        + " bytes missing");
                }
                if (c.equals(".."))
                {
                    b = backgroundTile;
                }
                else
                {
                    try
                    {
                        b = Integer.valueOf(c, 16);
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new GameException(
                            "Illegal data in tile map at y=" + y + " x=" + x);
                    }
                }
                tileMap.map[y][x] = b;
                i++;
            }
        }

        // Returns the newly created Tile Map
        return tileMap;
    }


    /**
     * Creates and returns a new Tile Map read from the specified input stream.
     * The stream must be positioned on the beginning of the tile map header
     * (Which is the 32 bit map size).
     *
     * @param stream
     *            The stream to read the tile map from
     * @param mapSize
     *            Defines the tilemap size. If this is 0 then the size is read
     *            from the tile map itself. This only works for the original
     *            game files. So if a map size != 0 is specified then it is
     *            assumed that we have to read Displacer's hacked data files.
     * @return The new Tile Map
     * @throws IOException
     *             When file operation fails.
     */

    public static TileMap read(final SeekableInputStream stream,
        final int mapSize) throws IOException
    {
        InputStream huffmanStream;
        int realMapSize;

        final boolean compressed = mapSize == 0;

        // Read map size from stream
        if (compressed)
        {
            realMapSize = stream.readSignedInt();
            if (realMapSize == 32 * 32)
            {
                realMapSize = 32;
            }
            else if (realMapSize == 64 * 64)
            {
                realMapSize = 64;
            }
            else
            {
                throw new IOException("Invalid Tile Map header");
            }
        }
        else
            realMapSize = mapSize;

        // Create the new TileMap
        final TileMap tileMap = new TileMap(realMapSize);

        // Read the unknown 32 bit value
        if (compressed)
        {
            tileMap.unknown = stream.readSignedInt();
            huffmanStream = new HuffmanInputStream(stream);
        }
        else
        {
            huffmanStream = stream;
        }

        // Read the tile map data
        for (int y = 0; y < realMapSize; y++)
        {
            for (int x = 0; x < realMapSize; x++)
            {
                tileMap.map[y][x] = huffmanStream.read();
            }
        }

        // Return the tile map
        return tileMap;
    }


    /**
     * Returns the tile map as XML.
     *
     * @param backgroundTile
     *            The background tile for which ".." is printed
     * @return The tile map as XML
     */

    public Element toXml(final int backgroundTile)
    {
        Element element;
        StringWriter text;
        PrintWriter writer;
        int mapSize;

        // Create the root element
        element = XmlUtils.createElement("tileMap");
        if (this.unknown != 0)
        {
            element.addAttribute("unknown", StringUtils.toHex(this.unknown));
        }

        // Determine the map size
        mapSize = this.map.length;

        // Write the tile map content
        text = new StringWriter();
        writer = new PrintWriter(text);
        writer.println();
        for (int y = 0; y < mapSize; y++)
        {
            writer.print("    ");
            for (int x = 0; x < mapSize; x++)
            {
                int b;

                if (x > 0)
                {
                    writer.print(" ");
                }

                b = this.map[y][x];
                if (b == backgroundTile)
                {
                    writer.append("..");
                }
                else
                {
                    writer.format("%02x", new Object[] { b });
                }
            }
            writer.println();
        }
        writer.print("  ");
        element.add(DocumentHelper.createText(text.toString()));

        // Return the XML code
        return element;
    }


    /**
     * Writes the Tile Map to the specified stream
     *
     * @param stream
     *            The stream to write the Tile Map to
     * @param compress
     *            If the tile map should be compressed
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final SeekableOutputStream stream, final boolean compress)
        throws IOException
    {
        int mapSize;
        ByteArrayOutputStream byteStream;
        byte[] bytes;
        HuffmanTree tree;
        HuffmanOutputStream huffmanStream;

        // Determine the map size
        mapSize = this.map.length;

        // Write the Tile Map header
        if (compress)
        {
            stream.writeInt(mapSize * mapSize);
            stream.writeInt(this.unknown);
        }

        // Write the Tile Map data
        byteStream = new ByteArrayOutputStream();
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                byteStream.write(this.map[y][x]);
            }
        }
        bytes = byteStream.toByteArray();
        if (compress)
        {
            tree = HuffmanTree.create(bytes);
            huffmanStream = new HuffmanOutputStream(stream, tree);
            huffmanStream.write(bytes);
            huffmanStream.flush();
        }
        else
        {
            stream.write(bytes);
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

    public int getTile(final int x, final int y)
    {
        return this.map[y][x];
    }


    /**
     * Sets the tile at the specified position.
     *
     * @param x
     *            The X coordinate
     * @param y
     *            The Y coordinate
     * @param tile
     *            The tile to set
     */

    public void setTile(final int x, final int y, final int tile)
    {
        this.map[y][x] = tile;
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
