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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameException;


/**
 * Action Selector Map part
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionSelectorMapPart extends AbstractPart
{
    /** The map size */
    private int mapSize;

    /** The action select map. First index is y, second is x */
    private int[][] map;

    /** The action class map */
    private ActionClassMapPart actionClassMap;


    /**
     * Creates an action selector map part from XML.
     * 
     * @param offset
     *            The offset
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     * @param actionClassMap
     *            The action class map
     */

    public ActionSelectorMapPart(int offset, Element element, int mapSize,
        ActionClassMapPart actionClassMap)
    {
        this(mapSize, actionClassMap);

        String data;
        String c;
        int i;
        int b;

        // Validate the offset
        if (offset != this.offset)
        {
            throw new GameException(String.format(
                "Action selector map is at the wrong offset. Should "
                    + "be at 0x%x but is at 0x%x", new Object[] { this.offset,
                    offset }));
        }

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
                    throw new GameException("Action selector map is corrupt: "
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
                    throw new GameException(
                        "Illegal data in action selector map at y=" + y + " x="
                            + x);
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
     * @param actionClassMap
     *            The action class map
     */

    public ActionSelectorMapPart(int mapSize, ActionClassMapPart actionClassMap)
    {
        super(mapSize * mapSize / 2, mapSize * mapSize);
        this.mapSize = mapSize;
        this.map = new int[mapSize][mapSize];
        this.actionClassMap = actionClassMap;
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The bytes of the game block
     * @param mapSize
     *            The map size
     * @param actionClassMap
     *            The action class map
     */

    public ActionSelectorMapPart(byte[] bytes, int mapSize,
        ActionClassMapPart actionClassMap)
    {
        this(mapSize, actionClassMap);
        parse(bytes, mapSize);
    }


    /**
     * Parses the action selector map
     * 
     * @param bytes
     *            The block bytes
     * @param mapSize
     *            The map size
     */

    private void parse(byte[] bytes, int mapSize)
    {
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                this.map[y][x] = bytes[this.offset + y * mapSize + x] & 0xff;
            }
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;
        StringWriter text;
        PrintWriter writer;

        element = DocumentHelper.createElement("actionSelectorMap");

        text = new StringWriter();
        writer = new PrintWriter(text);

        writer.println();
        for (int y = 0; y < this.mapSize; y++)
        {
            writer.print("    ");
            for (int x = 0; x < this.mapSize; x++)
            {
                int b;

                if (x > 0)
                {
                    writer.print(" ");
                }

                b = this.map[y][x];
                if (b == 0 && (this.actionClassMap.getActionClass(x, y) == 0))
                {
                    writer.print("..");
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
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream)
     */

    public void write(OutputStream stream) throws IOException
    {
        for (int y = 0; y < this.mapSize; y++)
        {
            for (int x = 0; x < this.mapSize; x++)
            {
                stream.write(this.map[y][x]);
            }
        }
    }


    /**
     * Returns the action selector for the specified position
     * 
     * @param x
     *            The X position
     * @param y
     *            The Y position
     * @return The action selector
     */

    public int getActionSelector(int x, int y)
    {
        return this.map[y][x];
    }
}
