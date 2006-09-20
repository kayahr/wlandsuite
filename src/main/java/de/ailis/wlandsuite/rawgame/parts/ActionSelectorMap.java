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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.ailis.wlandsuite.utils.XMLUtils;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Action Selector Map
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionSelectorMap extends AbstractPart
{
    /** The action select map. First index is y, second is x */
    private int[][] map;

    /** The action class map */
    private ActionClassMap actionClassMap;


    /**
     * Creates an action selector map part from XML.
     * 
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     * @param actionClassMap
     *            The action class map
     */

    public ActionSelectorMap(Element element, int mapSize,
        ActionClassMap actionClassMap)
    {
        this(mapSize, actionClassMap);

        String data;
        String c;
        int i;
        int b;

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

    public ActionSelectorMap(int mapSize, ActionClassMap actionClassMap)
    {
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

    public ActionSelectorMap(byte[] bytes, int mapSize,
        ActionClassMap actionClassMap)
    {
        this(mapSize, actionClassMap);

        this.offset = mapSize * mapSize / 2;
        this.size = mapSize * mapSize;
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                this.map[y][x] = bytes[this.offset + y * mapSize + x] & 0xff;
            }
        }
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;
        StringWriter text;
        PrintWriter writer;

        element = XMLUtils.createElement("actionSelectorMap");

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
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        for (int y = 0, yMax = this.map.length; y < yMax; y++)
        {
            for (int x = 0, xMax = this.map[y].length; x < xMax; x++)
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
